import { Module } from '@nestjs/common';
import { ConsolesController } from './consoles.controller';
import { ConsolesService } from './consoles.service';

@Module({
  controllers: [ConsolesController],
  providers: [ConsolesService],
  exports: [ConsolesService],
})
export class ConsolesModule {}
