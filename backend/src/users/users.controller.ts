import { Body, Controller, Get, Patch, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../auth/current-user.decorator';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';

@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class UsersController {
  constructor(private readonly users: UsersService) {}

  @Get('me')
  @ApiOperation({ summary: 'Get my profile' })
  async getMe(@CurrentUser('id') userId: string) {
    return this.users.getProfile(userId);
  }

  @Patch('me')
  @ApiOperation({ summary: 'Update my profile' })
  async updateMe(@CurrentUser('id') userId: string, @Body() dto: UpdateProfileDto) {
    return this.users.updateProfile(userId, dto);
  }
}
