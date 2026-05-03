import { Body, Controller, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RecognizeService } from './recognize.service';
import { RecognizeRequestDto, RecognizeResponseDto } from './dto/recognize.dto';

@ApiTags('recognize')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('recognize')
export class RecognizeController {
  constructor(private readonly recognize: RecognizeService) {}

  @Post('console')
  @ApiOperation({
    summary: 'Recognize a console / handheld / disc from 1-3 photos',
    description:
      'Submit images (URLs or data URIs) and receive a best-effort identification with rarity scoring. Does NOT add anything to the user collection — call POST /collection separately to confirm.',
  })
  async recognizeConsole(@Body() dto: RecognizeRequestDto): Promise<RecognizeResponseDto> {
    return this.recognize.recognize(dto);
  }
}
