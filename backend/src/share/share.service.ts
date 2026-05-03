import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { randomBytes } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { CollectionService } from '../collection/collection.service';

@Injectable()
export class ShareService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly collection: CollectionService,
  ) {}

  async createCollectionShare(userId: string) {
    const token = this.makeToken();
    await this.prisma.shareToken.create({
      data: { userId, scope: 'collection', token },
    });
    return { token, url: `/share/collection/${token}` };
  }

  async createItemShare(userId: string, itemId: string) {
    await this.collection.get(userId, itemId); // throws if not yours
    const token = this.makeToken();
    await this.prisma.shareToken.create({
      data: { userId, scope: `item:${itemId}`, token },
    });
    return { token, url: `/share/item/${token}` };
  }

  async resolveCollection(token: string) {
    const share = await this.prisma.shareToken.findUnique({ where: { token } });
    if (!share) throw new NotFoundException('Share link not found');
    if (share.scope !== 'collection') throw new ForbiddenException('Share scope mismatch');
    if (share.expiresAt && share.expiresAt < new Date()) {
      throw new NotFoundException('Share link expired');
    }
    const user = await this.prisma.user.findUnique({ where: { id: share.userId } });
    if (!user) throw new NotFoundException('Owner not found');
    const items = await this.collection.listForUserPublic(share.userId);
    return {
      owner: {
        id: user.id,
        displayName: user.displayName,
        city: user.city,
        country: user.country,
        avatarUrl: user.avatarUrl,
      },
      items,
    };
  }

  async resolveItem(token: string) {
    const share = await this.prisma.shareToken.findUnique({ where: { token } });
    if (!share) throw new NotFoundException('Share link not found');
    if (!share.scope.startsWith('item:')) throw new ForbiddenException('Share scope mismatch');
    const itemId = share.scope.slice('item:'.length);
    const item = await this.prisma.collectionItem.findUnique({
      where: { id: itemId },
      include: { consoleModel: true, user: true },
    });
    if (!item) throw new NotFoundException('Item not found');
    return {
      owner: {
        id: item.user.id,
        displayName: item.user.displayName,
        city: item.user.city,
        country: item.user.country,
        avatarUrl: item.user.avatarUrl,
      },
      item: {
        id: item.id,
        status: item.status,
        notes: item.notes,
        photos: item.photos,
        consoleModel: item.consoleModel,
        createdAt: item.createdAt,
      },
    };
  }

  private makeToken(): string {
    return randomBytes(16).toString('hex');
  }
}
