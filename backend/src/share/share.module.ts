import { Module } from '@nestjs/common';
import { CollectionModule } from '../collection/collection.module';
import { GameItemsModule } from '../game-items/game-items.module';
import { ShareController } from './share.controller';
import { ShareService } from './share.service';

@Module({
  imports: [CollectionModule, GameItemsModule],
  controllers: [ShareController],
  providers: [ShareService],
})
export class ShareModule {}
