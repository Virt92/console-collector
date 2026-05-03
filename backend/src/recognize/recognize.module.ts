import { Module } from '@nestjs/common';
import { ConsolesModule } from '../consoles/consoles.module';
import { GamesModule } from '../games/games.module';
import { RecognizeController } from './recognize.controller';
import { RecognizeService } from './recognize.service';
import { FalVisionClient } from './fal-vision.client';

@Module({
  imports: [ConsolesModule, GamesModule],
  controllers: [RecognizeController],
  providers: [RecognizeService, FalVisionClient],
  exports: [RecognizeService],
})
export class RecognizeModule {}
