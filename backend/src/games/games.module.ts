import { Module } from '@nestjs/common';
import { GamesController } from './games.controller';
import { GamesService } from './games.service';
import { IgdbClient } from './igdb.client';

@Module({
  controllers: [GamesController],
  providers: [GamesService, IgdbClient],
  exports: [GamesService],
})
export class GamesModule {}
