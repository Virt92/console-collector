import { PrismaClient, Rarity } from '@prisma/client';
import * as fs from 'fs';
import * as path from 'path';

const prisma = new PrismaClient();

interface SeedConsole {
  slug: string;
  name: string;
  manufacturer: string;
  year: number;
  region?: string;
  rarity: keyof typeof Rarity;
  imageUrl?: string;
  description?: string;
  aliases?: string[];
}

interface SeedAchievement {
  key: string;
  name: string;
  description: string;
}

const ACHIEVEMENTS: SeedAchievement[] = [
  { key: 'first_console', name: 'First Pickup', description: 'Add your very first console.' },
  { key: 'sony_collector', name: 'Sony Collector', description: 'Add 5 Sony consoles to your collection.' },
  { key: 'nintendo_collector', name: 'Nintendo Collector', description: 'Add 5 Nintendo consoles to your collection.' },
  { key: 'microsoft_collector', name: 'Xbox Loyalist', description: 'Add 5 Microsoft consoles to your collection.' },
  { key: 'sega_collector', name: 'Sega Does What...', description: 'Add 5 Sega consoles to your collection.' },
  { key: 'rare_hunter', name: 'Rare Hunter', description: 'Add a console with rarity RARE or higher.' },
  { key: 'epic_hunter', name: 'Epic Hunter', description: 'Add a console with rarity EPIC or higher.' },
  { key: 'legendary_hunter', name: 'Legendary Hunter', description: 'Add a console with LEGENDARY rarity.' },
  { key: 'retro_lover', name: 'Retro Lover', description: 'Add 3 consoles released before 1990.' },
  { key: 'modern_warrior', name: 'Modern Warrior', description: 'Own all current-gen consoles (PS5, Series X, Switch).' },
  { key: 'dedicated', name: 'Dedicated', description: 'Reach 10 items in your collection.' },
  { key: 'curator', name: 'Curator', description: 'Reach 25 items in your collection.' },
  { key: 'completionist', name: 'Completionist', description: 'Reach 50 items in your collection.' },
];

async function main() {
  const dataPath = path.resolve(__dirname, '../../data/consoles.json');
  const raw = fs.readFileSync(dataPath, 'utf-8');
  const consoles = JSON.parse(raw) as SeedConsole[];

  console.log(`Seeding ${consoles.length} consoles...`);
  for (const c of consoles) {
    await prisma.consoleModel.upsert({
      where: { slug: c.slug },
      update: {
        name: c.name,
        manufacturer: c.manufacturer,
        year: c.year,
        region: c.region,
        rarity: Rarity[c.rarity],
        imageUrl: c.imageUrl,
        description: c.description,
        aliases: c.aliases ?? [],
      },
      create: {
        slug: c.slug,
        name: c.name,
        manufacturer: c.manufacturer,
        year: c.year,
        region: c.region,
        rarity: Rarity[c.rarity],
        imageUrl: c.imageUrl,
        description: c.description,
        aliases: c.aliases ?? [],
      },
    });
  }

  console.log(`Seeding ${ACHIEVEMENTS.length} achievements...`);
  for (const a of ACHIEVEMENTS) {
    await prisma.achievement.upsert({
      where: { key: a.key },
      update: { name: a.name, description: a.description },
      create: a,
    });
  }

  console.log('Seed complete.');
}

main()
  .catch((err) => {
    console.error(err);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
