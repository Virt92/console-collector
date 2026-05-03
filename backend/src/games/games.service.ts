import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { IgdbClient } from './igdb.client';

export interface GameMatch {
  id: string;
  slug: string;
  title: string;
  platforms: string[];
  rarity: string;
  coverUrl: string | null;
}

@Injectable()
export class GamesService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly igdb: IgdbClient,
  ) {}

  async list(query?: { platform?: string; search?: string; take?: number }) {
    const where: Record<string, unknown> = {};
    if (query?.platform) where.platforms = { has: query.platform };
    if (query?.search) {
      where.OR = [
        { title: { contains: query.search, mode: 'insensitive' } },
        { developer: { contains: query.search, mode: 'insensitive' } },
        { publisher: { contains: query.search, mode: 'insensitive' } },
      ];
    }
    return this.prisma.game.findMany({
      where,
      orderBy: [{ releaseYear: 'asc' }, { title: 'asc' }],
      take: query?.take ?? 200,
    });
  }

  async getBySlug(slug: string) {
    const game = await this.prisma.game.findUnique({ where: { slug } });
    if (!game) throw new NotFoundException(`Game "${slug}" not found in catalog`);
    return game;
  }

  async getById(id: string) {
    const game = await this.prisma.game.findUnique({ where: { id } });
    if (!game) throw new NotFoundException(`Game with id "${id}" not found`);
    return game;
  }

  /**
   * Find the best catalog match for a vision-recognized title.
   * Strategy: exact slug, then alias / title contains, fall back to fuzzy score on title.
   */
  async findBestMatch(query: string, platform?: string): Promise<GameMatch | null> {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return null;

    const slugCandidate = this.toSlug(normalized);
    const slugMatch = await this.prisma.game.findUnique({ where: { slug: slugCandidate } });
    if (slugMatch && (!platform || slugMatch.platforms.includes(platform))) {
      return this.toMatch(slugMatch);
    }

    const all = await this.prisma.game.findMany();
    let best: { match: GameMatch; score: number } | null = null;
    for (const g of all) {
      const haystack = [g.title, g.slug, ...(g.aliases ?? [])]
        .filter(Boolean)
        .map((s) => s.toLowerCase());
      let score = 0;
      for (const h of haystack) {
        if (h === normalized) score = Math.max(score, 100);
        else if (h.includes(normalized) || normalized.includes(h)) {
          score = Math.max(score, 50 + Math.min(50, h.length));
        }
      }
      if (platform && g.platforms.includes(platform)) score += 5;
      if (score > 0 && (!best || score > best.score)) {
        best = { match: this.toMatch(g), score };
      }
    }
    return best?.match ?? null;
  }

  /**
   * If IGDB is configured, try to enrich the catalog by importing a game from IGDB.
   * Returns the newly-created or existing Game row, or null if IGDB lookup failed.
   */
  async importFromIgdb(query: string): Promise<GameMatch | null> {
    if (!this.igdb.isConfigured()) return null;
    const hit = await this.igdb.search(query);
    if (!hit) return null;

    const slug = hit.slug ?? this.toSlug(hit.name);
    const existing = await this.prisma.game.findFirst({
      where: { OR: [{ slug }, { igdbId: hit.id }] },
    });
    if (existing) return this.toMatch(existing);

    const created = await this.prisma.game.create({
      data: {
        slug,
        igdbId: hit.id,
        title: hit.name,
        platforms: hit.platforms,
        releaseYear: hit.releaseYear ?? null,
        publisher: null,
        developer: null,
        genres: hit.genres,
        coverUrl: hit.coverUrl,
        summary: hit.summary,
        rarity: 'COMMON',
        aliases: [],
      },
    });
    return this.toMatch(created);
  }

  private toMatch(g: {
    id: string;
    slug: string;
    title: string;
    platforms: string[];
    rarity: string;
    coverUrl: string | null;
  }): GameMatch {
    return {
      id: g.id,
      slug: g.slug,
      title: g.title,
      platforms: g.platforms,
      rarity: g.rarity,
      coverUrl: g.coverUrl,
    };
  }

  private toSlug(s: string): string {
    return s
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
}
