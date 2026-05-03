import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ItemStatus, Prisma } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { ItemStatusDto } from '../collection/dto/collection.dto';
import { CreateGameItemDto, UpdateGameItemDto } from './dto/game-items.dto';

@Injectable()
export class GameItemsService {
  constructor(private readonly prisma: PrismaService) {}

  async list(userId: string) {
    return this.prisma.gameItem.findMany({
      where: { userId },
      include: { game: true },
      orderBy: { createdAt: 'desc' },
    });
  }

  async listForUserPublic(userId: string) {
    return this.list(userId);
  }

  async get(userId: string, id: string) {
    const item = await this.prisma.gameItem.findUnique({
      where: { id },
      include: { game: true },
    });
    if (!item) throw new NotFoundException('Game item not found');
    if (item.userId !== userId) throw new ForbiddenException('Not your item');
    return item;
  }

  async create(userId: string, dto: CreateGameItemDto) {
    const game = await this.prisma.game.findUnique({ where: { id: dto.gameId } });
    if (!game) throw new NotFoundException('Game not found in catalog');
    if (game.platforms.length && !game.platforms.includes(dto.platformSlug)) {
      throw new BadRequestException(
        `Game "${game.title}" is not available on platform "${dto.platformSlug}". Known platforms: ${game.platforms.join(', ')}`,
      );
    }

    return this.prisma.gameItem.create({
      data: {
        userId,
        gameId: dto.gameId,
        platformSlug: dto.platformSlug,
        edition: dto.edition,
        region: dto.region,
        status: this.toStatus(dto.status),
        notes: dto.notes,
        photos: dto.photos ?? [],
        recognized: dto.recognized ? (dto.recognized as Prisma.InputJsonValue) : Prisma.JsonNull,
      },
      include: { game: true },
    });
  }

  async update(userId: string, id: string, dto: UpdateGameItemDto) {
    await this.get(userId, id);
    return this.prisma.gameItem.update({
      where: { id },
      data: {
        status: dto.status ? this.toStatus(dto.status) : undefined,
        edition: dto.edition,
        region: dto.region,
        notes: dto.notes,
      },
      include: { game: true },
    });
  }

  async remove(userId: string, id: string) {
    await this.get(userId, id);
    await this.prisma.gameItem.delete({ where: { id } });
    return { ok: true };
  }

  async stats(userId: string) {
    const items = await this.prisma.gameItem.findMany({
      where: { userId },
      include: { game: true },
    });
    const byRarity: Record<string, number> = {
      COMMON: 0,
      UNCOMMON: 0,
      RARE: 0,
      EPIC: 0,
      LEGENDARY: 0,
    };
    const byPlatform: Record<string, number> = {};
    for (const item of items) {
      byRarity[item.game.rarity] = (byRarity[item.game.rarity] ?? 0) + 1;
      byPlatform[item.platformSlug] = (byPlatform[item.platformSlug] ?? 0) + 1;
    }
    const totalCatalog = await this.prisma.game.count();
    return {
      total: items.length,
      uniqueGames: new Set(items.map((i) => i.gameId)).size,
      catalogSize: totalCatalog,
      byRarity,
      byPlatform,
    };
  }

  private toStatus(input?: ItemStatusDto): ItemStatus {
    switch (input) {
      case ItemStatusDto.FOR_SALE:
        return ItemStatus.FOR_SALE;
      case ItemStatusDto.FOR_TRADE:
        return ItemStatus.FOR_TRADE;
      case ItemStatusDto.GIVING_AWAY:
        return ItemStatus.GIVING_AWAY;
      case ItemStatusDto.OWNED:
      default:
        return ItemStatus.OWNED;
    }
  }
}
