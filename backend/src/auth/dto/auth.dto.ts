import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsNotEmpty, IsOptional, IsString, MaxLength, MinLength } from 'class-validator';

export class RegisterDto {
  @ApiProperty({ example: 'egor@example.com' })
  @IsEmail()
  email!: string;

  @ApiProperty({ example: 'P@ssw0rd!' })
  @IsString()
  @MinLength(6)
  @MaxLength(128)
  password!: string;

  @ApiProperty({ example: 'Egor' })
  @IsString()
  @IsNotEmpty()
  @MaxLength(64)
  displayName!: string;

  @ApiProperty({ example: 'Moscow', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(64)
  city?: string;

  @ApiProperty({ example: 'Russia', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(64)
  country?: string;
}

export class LoginDto {
  @ApiProperty({ example: 'egor@example.com' })
  @IsEmail()
  email!: string;

  @ApiProperty({ example: 'P@ssw0rd!' })
  @IsString()
  @MinLength(6)
  password!: string;
}

export class AuthResponseDto {
  @ApiProperty()
  accessToken!: string;

  @ApiProperty()
  user!: {
    id: string;
    email: string;
    displayName: string;
    city: string | null;
    country: string | null;
    avatarUrl: string | null;
  };
}
