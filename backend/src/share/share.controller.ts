import { Controller, Get, Param, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../auth/current-user.decorator';
import { ShareService } from './share.service';

@ApiTags('share')
@Controller('share')
export class ShareController {
  constructor(private readonly share: ShareService) {}

  @Post('collection')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Create a public share link for my entire collection' })
  shareCollection(@CurrentUser('id') userId: string) {
    return this.share.createCollectionShare(userId);
  }

  @Post('item/:itemId')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Create a public share link for a single card' })
  shareItem(@CurrentUser('id') userId: string, @Param('itemId') itemId: string) {
    return this.share.createItemShare(userId, itemId);
  }

  @Get('collection/:token')
  @ApiOperation({ summary: 'Resolve a public collection share link (no auth required)' })
  resolveCollection(@Param('token') token: string) {
    return this.share.resolveCollection(token);
  }

  @Get('item/:token')
  @ApiOperation({ summary: 'Resolve a public single-card share link (no auth required)' })
  resolveItem(@Param('token') token: string) {
    return this.share.resolveItem(token);
  }
}
