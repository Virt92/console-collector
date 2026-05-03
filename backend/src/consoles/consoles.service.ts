import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ConsolesService {
  constructor(private readonly prisma: PrismaService) {}

  async list(query?: { manufacturer?: string; search?: string }) {
    const where: Record<string, unknown> = {};
    if (query?.manufacturer) where.manufacturer = query.manufacturer;
    if (query?.search) {
      where.OR = [
        { name: { contains: query.search, mode: 'insensitive' } },
        { manufacturer: { contains: query.search, mode: 'insensitive' } },
      ];
    }
    return this.prisma.consoleModel.findMany({
      where,
      orderBy: [{ year: 'asc' }, { name: 'asc' }],
    });
  }

  async getBySlug(slug: string) {
    const model = await this.prisma.consoleModel.findUnique({ where: { slug } });
    if (!model) throw new NotFoundException(`Console "${slug}" not found in catalog`);
    return model;
  }

  async getById(id: string) {
    const model = await this.prisma.consoleModel.findUnique({ where: { id } });
    if (!model) throw new NotFoundException(`Console with id "${id}" not found in catalog`);
    return model;
  }

  async findBestMatch(query: string): Promise<{ id: string; slug: string; name: string } | null> {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return null;

    // Exact slug match.
    const slugMatch = await this.prisma.consoleModel.findUnique({
      where: { slug: this.toSlug(normalized) },
    });
    if (slugMatch) return slugMatch;

    // Alias match (case-insensitive contains).
    const all = await this.prisma.consoleModel.findMany();
    let best: { id: string; slug: string; name: string; score: number } | null = null;
    for (const c of all) {
      const haystack = [c.name, c.slug, ...(c.aliases ?? [])]
        .filter(Boolean)
        .map((s) => s.toLowerCase());
      let score = 0;
      for (const h of haystack) {
        if (h === normalized) score = Math.max(score, 100);
        else if (h.includes(normalized) || normalized.includes(h)) {
          score = Math.max(score, 50 + Math.min(50, h.length));
        }
      }
      if (score > 0 && (!best || score > best.score)) {
        best = { id: c.id, slug: c.slug, name: c.name, score };
      }
    }
    return best;
  }

  private toSlug(s: string): string {
    return s
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
}
