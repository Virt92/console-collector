import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface IgdbHit {
  id: number;
  name: string;
  slug?: string;
  platforms: string[];
  releaseYear?: number;
  coverUrl?: string;
  summary?: string;
  genres: string[];
}

interface IgdbApiGame {
  id: number;
  name: string;
  slug?: string;
  first_release_date?: number;
  summary?: string;
  cover?: { url?: string; image_id?: string };
  platforms?: { id: number; slug?: string; name?: string }[];
  genres?: { name: string }[];
}

/**
 * Minimal IGDB v4 client. IGDB requires Twitch OAuth credentials; if either
 * IGDB_CLIENT_ID or IGDB_CLIENT_SECRET is missing, the client reports
 * itself as unconfigured and search() always returns null. The /recognize/game
 * endpoint then falls back to local-catalog matching only.
 *
 * Docs: https://api-docs.igdb.com/
 */
@Injectable()
export class IgdbClient {
  private readonly logger = new Logger(IgdbClient.name);
  private readonly clientId?: string;
  private readonly clientSecret?: string;
  private accessToken: string | null = null;
  private accessTokenExpiresAt = 0;

  constructor(private readonly config: ConfigService) {
    this.clientId = config.get<string>('IGDB_CLIENT_ID');
    this.clientSecret = config.get<string>('IGDB_CLIENT_SECRET');
  }

  isConfigured(): boolean {
    return Boolean(this.clientId && this.clientSecret);
  }

  async search(query: string): Promise<IgdbHit | null> {
    if (!this.isConfigured()) return null;
    const token = await this.getToken();
    if (!token) return null;

    const body = `search "${query.replace(/"/g, '\\"')}"; fields name,slug,summary,first_release_date,platforms.slug,platforms.name,cover.image_id,genres.name; limit 1;`;
    const res = await fetch('https://api.igdb.com/v4/games', {
      method: 'POST',
      headers: {
        'Client-ID': this.clientId!,
        Authorization: `Bearer ${token}`,
        'Content-Type': 'text/plain',
      },
      body,
    });
    if (!res.ok) {
      this.logger.warn(`IGDB search failed (${res.status})`);
      return null;
    }
    const items = (await res.json()) as IgdbApiGame[];
    const hit = items[0];
    if (!hit) return null;
    return {
      id: hit.id,
      name: hit.name,
      slug: hit.slug,
      platforms: (hit.platforms ?? []).map((p) => p.slug ?? `igdb-${p.id}`),
      releaseYear: hit.first_release_date
        ? new Date(hit.first_release_date * 1000).getUTCFullYear()
        : undefined,
      coverUrl: hit.cover?.image_id
        ? `https://images.igdb.com/igdb/image/upload/t_cover_big/${hit.cover.image_id}.jpg`
        : undefined,
      summary: hit.summary,
      genres: (hit.genres ?? []).map((g) => g.name),
    };
  }

  private async getToken(): Promise<string | null> {
    if (this.accessToken && Date.now() < this.accessTokenExpiresAt) return this.accessToken;
    if (!this.clientId || !this.clientSecret) return null;

    const url = `https://id.twitch.tv/oauth2/token?client_id=${encodeURIComponent(this.clientId)}&client_secret=${encodeURIComponent(this.clientSecret)}&grant_type=client_credentials`;
    const res = await fetch(url, { method: 'POST' });
    if (!res.ok) {
      this.logger.warn(`IGDB auth failed (${res.status})`);
      return null;
    }
    const json = (await res.json()) as { access_token: string; expires_in: number };
    this.accessToken = json.access_token;
    this.accessTokenExpiresAt = Date.now() + Math.max(0, (json.expires_in - 60) * 1000);
    return this.accessToken;
  }
}
