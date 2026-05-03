import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { ItemStatus, Prisma } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import {
  CreateCollectionItemDto,
  ItemStatusDto,
  UpdateCollectionItemDto,
} from './dto/collection.dto';

@Injectable()
export class CollectionService {
  constructor(private readonly prisma: PrismaService) {}

  async list(userId: string) {
    return this.prisma.collectionItem.findMany({
      where: { userId },
      include: { consoleModel: true },
      orderBy: { createdAt: 'desc' },
    });
  }

  async listForUserPublic(userId: string) {
    // Public-friendly subset (used by share links).
    return this.prisma.collectionItem.findMany({
      where: { userId },
      include: { consoleModel: true },
      orderBy: { createdAt: 'desc' },
    });
  }

  async get(userId: string, id: string) {
    const item = await this.prisma.collectionItem.findUnique({
      where: { id },
      include: { consoleModel: true },
    });
    if (!item) throw new NotFoundException('Collection item not found');
    if (item.userId !== userId) throw new ForbiddenException('Not your item');
    return item;
  }

  async create(userId: string, dto: CreateCollectionItemDto) {
    const consoleModel = await this.prisma.consoleModel.findUnique({
      where: { id: dto.consoleModelId },
    });
    if (!consoleModel) throw new NotFoundException('Console model not found in catalog');

    return this.prisma.collectionItem.create({
      data: {
        userId,
        consoleModelId: dto.consoleModelId,
        status: this.toStatus(dto.status),
        notes: dto.notes,
        photos: dto.photos ?? [],
        recognized: dto.recognized
          ? (dto.recognized as Prisma.InputJsonValue)
          : Prisma.JsonNull,
      },
      include: { consoleModel: true },
    });
  }

  async update(userId: string, id: string, dto: UpdateCollectionItemDto) {
    await this.get(userId, id);
    return this.prisma.collectionItem.update({
      where: { id },
      data: {
        status: dto.status ? this.toStatus(dto.status) : undefined,
        notes: dto.notes,
      },
      include: { consoleModel: true },
    });
  }

  async remove(userId: string, id: string) {
    await this.get(userId, id);
    await this.prisma.collectionItem.delete({ where: { id } });
    return { ok: true };
  }

  async stats(userId: string) {
    const items = await this.prisma.collectionItem.findMany({
      where: { userId },
      include: { consoleModel: true },
    });
    const byRarity: Record<string, number> = {
      COMMON: 0,
      UNCOMMON: 0,
      RARE: 0,
      EPIC: 0,
      LEGENDARY: 0,
    };
    for (const item of items) {
      byRarity[item.consoleModel.rarity] = (byRarity[item.consoleModel.rarity] ?? 0) + 1;
    }
    const totalCatalog = await this.prisma.consoleModel.count();
    return {
      total: items.length,
      uniqueModels: new Set(items.map((i) => i.consoleModelId)).size,
      catalogSize: totalCatalog,
      byRarity,
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
