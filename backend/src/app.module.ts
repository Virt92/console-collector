import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { ConsolesModule } from './consoles/consoles.module';
import { CollectionModule } from './collection/collection.module';
import { GamesModule } from './games/games.module';
import { GameItemsModule } from './game-items/game-items.module';
import { RecognizeModule } from './recognize/recognize.module';
import { ShareModule } from './share/share.module';
import { HealthController } from './health/health.controller';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    PrismaModule,
    AuthModule,
    UsersModule,
    ConsolesModule,
    CollectionModule,
    GamesModule,
    GameItemsModule,
    RecognizeModule,
    ShareModule,
  ],
  controllers: [HealthController],
})
export class AppModule {}
