import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../auth/current-user.decorator';
import { GameItemsService } from './game-items.service';
import { CreateGameItemDto, UpdateGameItemDto } from './dto/game-items.dto';

@ApiTags('game-items')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('collection/games')
export class GameItemsController {
  constructor(private readonly gameItems: GameItemsService) {}

  @Get()
  @ApiOperation({ summary: 'List the games I own.' })
  list(@CurrentUser('id') userId: string) {
    return this.gameItems.list(userId);
  }

  @Get('stats')
  @ApiOperation({ summary: 'Aggregate stats for game items.' })
  stats(@CurrentUser('id') userId: string) {
    return this.gameItems.stats(userId);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get one game item.' })
  get(@CurrentUser('id') userId: string, @Param('id') id: string) {
    return this.gameItems.get(userId, id);
  }

  @Post()
  @ApiOperation({ summary: 'Add a game I own (after disc recognition).' })
  create(@CurrentUser('id') userId: string, @Body() dto: CreateGameItemDto) {
    return this.gameItems.create(userId, dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update a game item (status, edition, region, notes).' })
  update(
    @CurrentUser('id') userId: string,
    @Param('id') id: string,
    @Body() dto: UpdateGameItemDto,
  ) {
    return this.gameItems.update(userId, id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Remove a game from my collection.' })
  remove(@CurrentUser('id') userId: string, @Param('id') id: string) {
    return this.gameItems.remove(userId, id);
  }
}
