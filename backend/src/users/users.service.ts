import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';

@Injectable()
export class UsersService {
  constructor(private readonly prisma: PrismaService) {}

  async getProfile(userId: string) {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user) throw new NotFoundException('User not found');
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      city: user.city,
      country: user.country,
      avatarUrl: user.avatarUrl,
      bio: user.bio,
    };
  }

  async updateProfile(userId: string, dto: UpdateProfileDto) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { ...dto },
    });
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      city: user.city,
      country: user.country,
      avatarUrl: user.avatarUrl,
      bio: user.bio,
    };
  }
}
