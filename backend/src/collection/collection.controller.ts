import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../auth/current-user.decorator';
import { CollectionService } from './collection.service';
import { CreateCollectionItemDto, UpdateCollectionItemDto } from './dto/collection.dto';

@ApiTags('collection')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('collection')
export class CollectionController {
  constructor(private readonly collection: CollectionService) {}

  @Get()
  @ApiOperation({ summary: 'List my collection' })
  list(@CurrentUser('id') userId: string) {
    return this.collection.list(userId);
  }

  @Get('stats')
  @ApiOperation({ summary: 'Aggregate stats: total, by-rarity, catalog completion' })
  stats(@CurrentUser('id') userId: string) {
    return this.collection.stats(userId);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get one collection item' })
  get(@CurrentUser('id') userId: string, @Param('id') id: string) {
    return this.collection.get(userId, id);
  }

  @Post()
  @ApiOperation({ summary: 'Add a console to my collection (after recognition)' })
  create(@CurrentUser('id') userId: string, @Body() dto: CreateCollectionItemDto) {
    return this.collection.create(userId, dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update a collection item (status, notes)' })
  update(
    @CurrentUser('id') userId: string,
    @Param('id') id: string,
    @Body() dto: UpdateCollectionItemDto,
  ) {
    return this.collection.update(userId, id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Remove an item from my collection' })
  remove(@CurrentUser('id') userId: string, @Param('id') id: string) {
    return this.collection.remove(userId, id);
  }
}
