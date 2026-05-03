import { ApiProperty } from '@nestjs/swagger';
import { ArrayMaxSize, ArrayMinSize, IsArray, IsString } from 'class-validator';

export class RecognizeRequestDto {
  @ApiProperty({
    description:
      'Up to 3 image inputs. Each item can be a public URL (https://...) or a data URI (data:image/jpeg;base64,...).',
    example: [
      'https://example.com/console-front.jpg',
      'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQE...',
    ],
  })
  @IsArray()
  @ArrayMinSize(1)
  @ArrayMaxSize(3)
  @IsString({ each: true })
  images!: string[];
}

export class RecognizeResponseDto {
  @ApiProperty({ example: 'PlayStation 5' })
  consoleName!: string;

  @ApiProperty({ example: 'playstation-5' })
  consoleSlug!: string;

  @ApiProperty({ example: 'cuid-or-uuid' })
  consoleModelId!: string | null;

  @ApiProperty({ example: 'COMMON' })
  rarity!: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';

  @ApiProperty({ example: 0.92 })
  confidence!: number;

  @ApiProperty({ example: 'Distinctive white side panels and disc drive visible.' })
  reasoning!: string;

  @ApiProperty({
    description: 'Free-form notes about edition / variant / condition observed.',
    example: { variant: 'Disc Edition', condition: 'mint' },
  })
  details!: Record<string, unknown>;
}
