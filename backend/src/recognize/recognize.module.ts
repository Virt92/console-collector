import { Module } from '@nestjs/common';
import { ConsolesModule } from '../consoles/consoles.module';
import { RecognizeController } from './recognize.controller';
import { RecognizeService } from './recognize.service';
import { FalVisionClient } from './fal-vision.client';

@Module({
  imports: [ConsolesModule],
  controllers: [RecognizeController],
  providers: [RecognizeService, FalVisionClient],
  exports: [RecognizeService],
})
export class RecognizeModule {}
