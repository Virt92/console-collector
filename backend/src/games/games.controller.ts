import { Controller, Get, Param, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { GamesService } from './games.service';

@ApiTags('games')
@Controller('games')
export class GamesController {
  constructor(private readonly games: GamesService) {}

  @Get()
  @ApiOperation({ summary: 'List the local game catalog (filter by platform / search).' })
  list(@Query('platform') platform?: string, @Query('search') search?: string) {
    return this.games.list({ platform, search });
  }

  @Get(':slug')
  @ApiOperation({ summary: 'Get a game by slug (e.g. "halo-3").' })
  getBySlug(@Param('slug') slug: string) {
    return this.games.getBySlug(slug);
  }
}
