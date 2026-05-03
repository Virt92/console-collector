import { Module } from '@nestjs/common';
import { GameItemsController } from './game-items.controller';
import { GameItemsService } from './game-items.service';

@Module({
  controllers: [GameItemsController],
  providers: [GameItemsService],
  exports: [GameItemsService],
})
export class GameItemsModule {}
