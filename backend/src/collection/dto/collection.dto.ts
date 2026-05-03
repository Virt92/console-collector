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

export enum ItemStatusDto {
  OWNED = 'OWNED',
  FOR_SALE = 'FOR_SALE',
  FOR_TRADE = 'FOR_TRADE',
  GIVING_AWAY = 'GIVING_AWAY',
}

export class CreateCollectionItemDto {
  @ApiProperty({ description: 'Console catalog id (UUID)' })
  @IsString()
  consoleModelId!: string;

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

  @ApiProperty({
    required: false,
    description: 'Raw output from the recognition step (variant, condition, reasoning, etc.).',
  })
  @IsOptional()
  @IsObject()
  recognized?: Record<string, unknown>;
}

export class UpdateCollectionItemDto {
  @ApiProperty({ enum: ItemStatusDto, required: false })
  @IsOptional()
  @IsEnum(ItemStatusDto)
  status?: ItemStatusDto;

  @ApiProperty({ required: false, maxLength: 1000 })
  @IsOptional()
  @IsString()
  @MaxLength(1000)
  notes?: string;
}
