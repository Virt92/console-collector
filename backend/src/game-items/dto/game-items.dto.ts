import { ApiProperty } from '@nestjs/swagger';
import {
  ArrayMaxSize,
  IsArray,
  IsEnum,
  IsObject,
  IsOptional,
  IsString,
  MaxLength,
} from 'class-validator';
import { ItemStatusDto } from '../../collection/dto/collection.dto';

export class CreateGameItemDto {
  @ApiProperty({ description: 'Game catalog id (UUID)' })
  @IsString()
  gameId!: string;

  @ApiProperty({
    description:
      'Slug of the platform the user owns the game on (must be one of the game.platforms entries, e.g. "playstation-4").',
  })
  @IsString()
  platformSlug!: string;

  @ApiProperty({ required: false, description: 'Edition / variant, e.g. "Collector\'s Edition".' })
  @IsOptional()
  @IsString()
  @MaxLength(120)
  edition?: string;

  @ApiProperty({ required: false, description: 'Region marker, e.g. "PAL", "NTSC-U", "JP".' })
  @IsOptional()
  @IsString()
  @MaxLength(40)
  region?: string;

  @ApiProperty({ enum: ItemStatusDto, default: ItemStatusDto.OWNED, required: false })
  @IsOptional()
  @IsEnum(ItemStatusDto)
  status?: ItemStatusDto;

  @ApiProperty({ required: false, maxLength: 1000 })
  @IsOptional()
  @IsString()
  @MaxLength(1000)
  notes?: string;

  @ApiProperty({ required: false, type: [String], description: 'Up to 5 photo URLs.' })
  @IsOptional()
  @IsArray()
  @ArrayMaxSize(5)
  @IsString({ each: true })
  photos?: string[];

  @ApiProperty({ required: false, description: 'Raw recognition payload.' })
  @IsOptional()
  @IsObject()
  recognized?: Record<string, unknown>;
}

export class UpdateGameItemDto {
  @ApiProperty({ enum: ItemStatusDto, required: false })
  @IsOptional()
  @IsEnum(ItemStatusDto)
  status?: ItemStatusDto;

  @ApiProperty({ required: false })
  @IsOptional()
  @IsString()
  @MaxLength(120)
  edition?: string;

  @ApiProperty({ required: false })
  @IsOptional()
  @IsString()
  @MaxLength(40)
  region?: string;

  @ApiProperty({ required: false, maxLength: 1000 })
  @IsOptional()
  @IsString()
  @MaxLength(1000)
  notes?: string;
}
