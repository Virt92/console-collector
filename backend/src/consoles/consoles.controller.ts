import { Controller, Get, Param, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { ConsolesService } from './consoles.service';

@ApiTags('consoles')
@Controller('consoles')
export class ConsolesController {
  constructor(private readonly consoles: ConsolesService) {}

  @Get()
  @ApiOperation({ summary: 'List all known consoles in the catalog' })
  list(@Query('manufacturer') manufacturer?: string, @Query('search') search?: string) {
    return this.consoles.list({ manufacturer, search });
  }

  @Get(':slug')
  @ApiOperation({ summary: 'Get a console by slug (e.g. "playstation-5")' })
  getBySlug(@Param('slug') slug: string) {
    return this.consoles.getBySlug(slug);
  }
}
