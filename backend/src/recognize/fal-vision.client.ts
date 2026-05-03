import { Injectable, InternalServerErrorException, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface FalVisionResponse {
  output: string;
  raw: unknown;
}

/**
 * Thin client over fal.ai's `fal-ai/any-llm/vision` endpoint.
 * Docs: https://fal.ai/models/fal-ai/any-llm/vision/api
 *
 * The endpoint accepts one or more image URLs (or data URIs) and a prompt,
 * and returns a text completion from the configured underlying VLM.
 */
@Injectable()
export class FalVisionClient {
  private readonly logger = new Logger(FalVisionClient.name);
  private readonly endpoint = 'https://fal.run/fal-ai/any-llm/vision';
  private readonly model: string;

  constructor(private readonly config: ConfigService) {
    this.model = config.get<string>('FAL_VISION_MODEL') ?? 'google/gemini-flash-1.5';
  }

  async complete(prompt: string, imageInputs: string[]): Promise<FalVisionResponse> {
    const apiKey = this.config.get<string>('FAL_API_KEY');
    if (!apiKey) {
      throw new InternalServerErrorException(
        'FAL_API_KEY is not configured on the backend. Set it in .env to enable vision recognition.',
      );
    }

    const body = {
      prompt,
      image_url: imageInputs[0],
      image_urls: imageInputs,
      model: this.model,
    };

    const res = await fetch(this.endpoint, {
      method: 'POST',
      headers: {
        Authorization: `Key ${apiKey}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      this.logger.error(`fal.ai vision request failed (${res.status}): ${text}`);
      throw new InternalServerErrorException(
        `Vision provider returned HTTP ${res.status}. Check FAL_API_KEY and model availability.`,
      );
    }

    const json = (await res.json()) as Record<string, unknown>;
    const output =
      (json.output as string | undefined) ??
      (typeof json.text === 'string' ? json.text : undefined) ??
      JSON.stringify(json);

    return { output, raw: json };
  }
}
