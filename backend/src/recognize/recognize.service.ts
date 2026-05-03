import { BadRequestException, Injectable, Logger } from '@nestjs/common';
import { ConsolesService } from '../consoles/consoles.service';
import { FalVisionClient } from './fal-vision.client';
import { RecognizeRequestDto, RecognizeResponseDto } from './dto/recognize.dto';

const SYSTEM_PROMPT = `You are an expert in video game consoles, gaming hardware, and game discs from 1972 to today.
You will receive 1-3 photos of a single object (a console, handheld, or game disc/case).
Your job: identify exactly what it is, including any limited / collector edition.

Return STRICT JSON, no prose, matching this schema:
{
  "consoleName": "<canonical product name, e.g. 'PlayStation 5 Slim Disc Edition' or 'Game Boy Color (Atomic Purple)' or 'Xbox 360 Slim'>",
  "consoleSlug": "<lowercase-kebab-case slug, e.g. 'playstation-5-slim'>",
  "rarity": "COMMON" | "UNCOMMON" | "RARE" | "EPIC" | "LEGENDARY",
  "confidence": <number between 0 and 1>,
  "reasoning": "<one or two short sentences explaining what visual cues led you to this conclusion>",
  "variant": "<optional, e.g. 'Disc Edition', '20th Anniversary', 'Pikachu Edition'>",
  "condition": "<optional: mint | good | used | damaged | unknown>",
  "kind": "console" | "handheld" | "disc" | "cartridge" | "accessory" | "unknown"
}

Rarity guidance:
- COMMON: standard mass-produced retail editions (e.g. plain PS5, plain Xbox Series X).
- UNCOMMON: regional or first-print variants of common consoles, popular bundles.
- RARE: confirmed limited editions with print runs >50k (e.g. 20th Anniversary PS4).
- EPIC: very limited editions, regional exclusives, retailer exclusives <50k.
- LEGENDARY: numbered limited editions <10k, prototypes, dev kits, store displays.

If you cannot identify the object with confidence, return rarity COMMON, confidence < 0.3, and explain why.
Return ONLY the JSON object, no markdown fences.`;

interface VisionResult {
  consoleName: string;
  consoleSlug: string;
  rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  confidence: number;
  reasoning: string;
  variant?: string;
  condition?: string;
  kind?: string;
}

@Injectable()
export class RecognizeService {
  private readonly logger = new Logger(RecognizeService.name);

  constructor(
    private readonly fal: FalVisionClient,
    private readonly consoles: ConsolesService,
  ) {}

  async recognize(dto: RecognizeRequestDto): Promise<RecognizeResponseDto> {
    if (!dto.images?.length) {
      throw new BadRequestException('At least one image is required');
    }

    const { output, raw } = await this.fal.complete(SYSTEM_PROMPT, dto.images);
    const parsed = this.parseVisionOutput(output);

    const matched = await this.consoles.findBestMatch(parsed.consoleName);

    return {
      consoleName: parsed.consoleName,
      consoleSlug: matched?.slug ?? parsed.consoleSlug,
      consoleModelId: matched?.id ?? null,
      rarity: parsed.rarity,
      confidence: parsed.confidence,
      reasoning: parsed.reasoning,
      details: {
        variant: parsed.variant,
        condition: parsed.condition,
        kind: parsed.kind,
        rawOutput: typeof raw === 'object' ? raw : output,
      },
    };
  }

  private parseVisionOutput(raw: string): VisionResult {
    const stripped = raw.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '');
    const start = stripped.indexOf('{');
    const end = stripped.lastIndexOf('}');
    if (start < 0 || end < start) {
      this.logger.warn(`Vision output was not JSON: ${stripped.slice(0, 200)}`);
      return {
        consoleName: 'Unknown object',
        consoleSlug: 'unknown',
        rarity: 'COMMON',
        confidence: 0,
        reasoning: 'Vision model returned a non-JSON response.',
      };
    }
    const slice = stripped.slice(start, end + 1);
    try {
      const parsed = JSON.parse(slice) as Partial<VisionResult>;
      const rarity = this.normalizeRarity(parsed.rarity);
      return {
        consoleName: parsed.consoleName ?? 'Unknown',
        consoleSlug: parsed.consoleSlug ?? this.toSlug(parsed.consoleName ?? 'unknown'),
        rarity,
        confidence: this.clamp(Number(parsed.confidence ?? 0), 0, 1),
        reasoning: parsed.reasoning ?? '',
        variant: parsed.variant,
        condition: parsed.condition,
        kind: parsed.kind,
      };
    } catch (err) {
      this.logger.warn(`Failed to parse vision JSON: ${(err as Error).message}`);
      return {
        consoleName: 'Unknown object',
        consoleSlug: 'unknown',
        rarity: 'COMMON',
        confidence: 0,
        reasoning: 'Vision model returned malformed JSON.',
      };
    }
  }

  private normalizeRarity(input: unknown): VisionResult['rarity'] {
    const allowed: VisionResult['rarity'][] = [
      'COMMON',
      'UNCOMMON',
      'RARE',
      'EPIC',
      'LEGENDARY',
    ];
    const upper = String(input ?? '').toUpperCase();
    return (allowed as string[]).includes(upper) ? (upper as VisionResult['rarity']) : 'COMMON';
  }

  private toSlug(s: string): string {
    return s
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

  private clamp(n: number, min: number, max: number): number {
    if (Number.isNaN(n)) return min;
    return Math.max(min, Math.min(max, n));
  }
}
