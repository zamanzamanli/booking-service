# Database Migrations

Uses **Flyway** - runs SQL files automatically on app startup.

## File Naming

```
V{timestamp}__{description}.sql
```

Examples:
- `V20251120120000__create_demos_table.sql`
- `V20251120120100__insert_sample_data.sql`

Use timestamp format: `YYYYMMDDHHmmss`

## Adding Migrations

1. Create file with current timestamp: `V20251120150000__add_column.sql`
2. Write SQL
3. Restart app

Never modify existing migration files after they've been applied.

## Reset Database (Dev Only)

```bash
docker-compose down -v && docker-compose up -d
```
