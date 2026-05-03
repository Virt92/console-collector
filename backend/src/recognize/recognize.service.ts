import { BadRequestException, Injectable, Logger } from '@nestjs/common';
import { ConsolesService } from '../consoles/consoles.service';
import { GamesService } from '../games/games.service';
import { FalVisionClient } from './fal-vision.client';
import {
  RecognizeGameResponseDto,
  RecognizeRequestDto,
  RecognizeResponseDto,
} from './dto/recognize.dto';

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

const GAME_PROMPT = `You are an expert in video game discs, cartridges, and box art across every platform from 1972 to today.
You will receive 1-3 photos of a single game (a disc, cartridge, or box).
Your job: identify the game title, the platform it is for, the region, and any limited / collector edition.

Return STRICT JSON, no prose, matching this schema:
{
  "title": "<canonical game title, e.g. 'The Legend of Zelda: Breath of the Wild'>",
  "slug": "<lowercase-kebab-case slug, e.g. 'the-legend-of-zelda-breath-of-the-wild'>",
  "platformSlug": "<lowercase-kebab-case platform slug, e.g. 'switch', 'playstation-4', 'xbox-360', 'nintendo-64'>",
  "platformName": "<human-readable platform, e.g. 'Nintendo Switch'>",
  "region": "<one of: PAL, NTSC-U, NTSC-J, JP, EU, US, KR, AS, unknown>",
  "edition": "<optional, e.g. 'Standard', 'Collector\u2019s Edition', 'Game of the Year', 'Limited Edition'>",
  "rarity": "COMMON" | "UNCOMMON" | "RARE" | "EPIC" | "LEGENDARY",
  "confidence": <number between 0 and 1>,
  "reasoning": "<one or two short sentences explaining what visual cues led you to this conclusion>",
  "kind": "disc" | "cartridge" | "box" | "digital-key" | "unknown"
}

Rarity guidance for games:
- COMMON: standard retail releases, GOTY editions, common store exclusives.
- UNCOMMON: regional first prints, sealed greatest-hits/platinum releases.
- RARE: confirmed limited editions, complete-in-box for retro titles, popular collector's editions.
- EPIC: low-print collector's bundles, sealed retro classics, regional exclusives.
- LEGENDARY: ultra-rare titles like Stadium Events, Panzer Dragoon Saga, EarthBound CIB, prototypes, store displays.

If you cannot identify the game with confidence, return rarity COMMON, confidence < 0.3, and explain why.
Return ONLY the JSON object, no markdown fences.`;

type RarityName = 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';

interface VisionResult {
  consoleName: string;
  consoleSlug: string;
  rarity: RarityName;
  confidence: number;
  reasoning: string;
  variant?: string;
  condition?: string;
  kind?: string;
}

interface GameVisionResult {
  title: string;
  slug: string;
  platformSlug: string;
  platformName?: string;
  region?: string;
  edition?: string;
  rarity: RarityName;
  confidence: number;
  reasoning: string;
  kind?: string;
}

@Injectable()
export class RecognizeService {
  private readonly logger = new Logger(RecognizeService.name);

  constructor(
    private readonly fal: FalVisionClient,
    private readonly consoles: ConsolesService,
    private readonly games: GamesService,
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

  async recognizeGame(dto: RecognizeRequestDto): Promise<RecognizeGameResponseDto> {
    if (!dto.images?.length) {
      throw new BadRequestException('At least one image is required');
    }

    const { output, raw } = await this.fal.complete(GAME_PROMPT, dto.images);
    const parsed = this.parseGameOutput(output);

    let matched = await this.games.findBestMatch(parsed.title, parsed.platformSlug);
    if (!matched) {
      matched = await this.games.importFromIgdb(parsed.title);
    }

    return {
      title: parsed.title,
      slug: matched?.slug ?? parsed.slug,
      gameId: matched?.id ?? null,
      coverUrl: matched?.coverUrl ?? null,
      platformSlug: parsed.platformSlug,
      platformName: parsed.platformName,
      region: parsed.region,
      edition: parsed.edition,
      rarity: parsed.rarity,
      confidence: parsed.confidence,
      reasoning: parsed.reasoning,
      details: {
        kind: parsed.kind,
        rawOutput: typeof raw === 'object' ? raw : output,
      },
    };
  }

  private parseGameOutput(raw: string): GameVisionResult {
    const slice = this.extractJsonObject(raw);
    if (!slice) {
      this.logger.warn(`Game vision output was not JSON: ${raw.slice(0, 200)}`);
      return {
        title: 'Unknown game',
        slug: 'unknown',
        platformSlug: 'unknown',
        rarity: 'COMMON',
        confidence: 0,
        reasoning: 'Vision model returned a non-JSON response.',
      };
    }
    try {
      const parsed = JSON.parse(slice) as Partial<GameVisionResult>;
      return {
        title: parsed.title ?? 'Unknown',
        slug: parsed.slug ?? this.toSlug(parsed.title ?? 'unknown'),
        platformSlug: parsed.platformSlug ?? 'unknown',
        platformName: parsed.platformName,
        region: parsed.region,
        edition: parsed.edition,
        rarity: this.normalizeRarity(parsed.rarity),
        confidence: this.clamp(Number(parsed.confidence ?? 0), 0, 1),
        reasoning: parsed.reasoning ?? '',
        kind: parsed.kind,
      };
    } catch (err) {
      this.logger.warn(`Failed to parse game vision JSON: ${(err as Error).message}`);
      return {
        title: 'Unknown game',
        slug: 'unknown',
        platformSlug: 'unknown',
        rarity: 'COMMON',
        confidence: 0,
        reasoning: 'Vision model returned malformed JSON.',
      };
    }
  }

  private extractJsonObject(raw: string): string | null {
    const stripped = raw
      .trim()
      .replace(/^```(?:json)?\s*/i, '')
      .replace(/\s*```$/i, '');
    const start = stripped.indexOf('{');
    const end = stripped.lastIndexOf('}');
    if (start < 0 || end < start) return null;
    return stripped.slice(start, end + 1);
  }

  private parseVisionOutput(raw: string): VisionResult {
    const slice = this.extractJsonObject(raw);
    if (!slice) {
      this.logger.warn(`Vision output was not JSON: ${raw.slice(0, 200)}`);
      return {
        consoleName: 'Unknown object',
        consoleSlug: 'unknown',
        rarity: 'COMMON',
        confidence: 0,
        reasoning: 'Vision model returned a non-JSON response.',
      };
    }
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

  private normalizeRarity(input: unknown): RarityName {
    const allowed: RarityName[] = ['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY'];
    const upper = String(input ?? '').toUpperCase();
    return (allowed as string[]).includes(upper) ? (upper as RarityName) : 'COMMON';
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
