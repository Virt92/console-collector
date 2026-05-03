import { Module } from '@nestjs/common';
import { CollectionModule } from '../collection/collection.module';
import { ShareController } from './share.controller';
import { ShareService } from './share.service';

@Module({
  imports: [CollectionModule],
  controllers: [ShareController],
  providers: [ShareService],
})
export class ShareModule {}
