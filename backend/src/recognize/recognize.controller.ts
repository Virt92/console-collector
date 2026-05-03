import { Body, Controller, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RecognizeService } from './recognize.service';
import {
  RecognizeGameResponseDto,
  RecognizeRequestDto,
  RecognizeResponseDto,
} from './dto/recognize.dto';

@ApiTags('recognize')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('recognize')
export class RecognizeController {
  constructor(private readonly recognize: RecognizeService) {}

  @Post('console')
  @ApiOperation({
    summary: 'Recognize a console / handheld from 1-3 photos',
    description:
      'Submit images (URLs or data URIs) and receive a best-effort identification with rarity scoring. Does NOT add anything to the user collection — call POST /collection separately to confirm.',
  })
  async recognizeConsole(@Body() dto: RecognizeRequestDto): Promise<RecognizeResponseDto> {
    return this.recognize.recognize(dto);
  }

  @Post('game')
  @ApiOperation({
    summary: 'Recognize a game disc / cartridge / box from 1-3 photos',
    description:
      'Submit images and receive a best-effort identification of the game title, platform, and rarity. ' +
      'If the recognized title exists in the local catalog (or can be imported from IGDB when configured), ' +
      'a `gameId` is returned. Does NOT add anything to the user collection — call POST /collection/games to confirm.',
  })
  async recognizeGame(@Body() dto: RecognizeRequestDto): Promise<RecognizeGameResponseDto> {
    return this.recognize.recognizeGame(dto);
  }
}
