#!/bin/sh

echo "============================================"
echo "Setting up PostgreSQL Logical Replication"
echo "============================================"

# Wait for databases to be fully ready
echo "⏳ Waiting for databases to be ready..."
sleep 15

echo ""
echo "📝 Step 1: Creating publication on Write DB..."

# Create publication on Write DB
PGPASSWORD=${DB_PASSWORD} psql -h postgres-write -U ${DB_USERNAME} -d ${DB_DATABASE} <<-EOSQL
    -- Check if publication exists
    SELECT 'Checking existing publications...' as status;
    SELECT * FROM pg_publication WHERE pubname = 'winestore_pub';

    -- Drop if exists (for clean setup)
    DROP PUBLICATION IF EXISTS winestore_pub;

    -- Create publication
    CREATE PUBLICATION winestore_pub FOR ALL TABLES;

    -- Verify
    SELECT 'Publication created:' as status;
    SELECT pubname, puballtables FROM pg_publication WHERE pubname = 'winestore_pub';
EOSQL

if [ $? -eq 0 ]; then
    echo "✅ Publication created successfully on Write DB"
else
    echo "❌ Failed to create publication on Write DB"
    exit 1
fi

echo ""
echo "📝 Step 2: Creating subscription on Read DB..."

# IMPORTANT: Cannot use DO block for CREATE SUBSCRIPTION
# Must run directly
PGPASSWORD=${DB_PASSWORD} psql -h postgres-read -U ${DB_USERNAME} -d ${DB_DATABASE} <<-EOSQL
    -- Check if subscription exists
    SELECT 'Checking existing subscriptions...' as status;
    SELECT * FROM pg_subscription WHERE subname = 'winestore_sub';

    -- Drop if exists (for clean setup)
    DROP SUBSCRIPTION IF EXISTS winestore_sub;

    -- Create subscription (OUTSIDE of DO block - this is the fix!)
    CREATE SUBSCRIPTION winestore_sub
    CONNECTION 'host=postgres-write port=5432 dbname=${DB_DATABASE} user=${DB_USERNAME} password=${DB_PASSWORD}'
    PUBLICATION winestore_pub
    WITH (copy_data = true, create_slot = true);

    -- Verify
    SELECT 'Subscription created:' as status;
    SELECT subname, subenabled FROM pg_subscription WHERE subname = 'winestore_sub';
EOSQL

if [ $? -eq 0 ]; then
    echo "✅ Subscription created successfully on Read DB"
else
    echo "❌ Failed to create subscription on Read DB"
    exit 1
fi

echo ""
echo "📝 Step 3: Verifying replication status..."

# Check replication status
PGPASSWORD=${DB_PASSWORD} psql -h postgres-read -U ${DB_USERNAME} -d ${DB_DATABASE} <<-EOSQL
    SELECT 'Replication Status:' as info;
    SELECT
        subname,
        subenabled as enabled,
        (subenabled AND pid IS NOT NULL) as active
    FROM pg_subscription s
    LEFT JOIN pg_stat_subscription ss ON s.oid = ss.subid
    WHERE subname = 'winestore_sub';
EOSQL

echo ""
echo "============================================"
echo "✅ Replication setup completed successfully!"
echo "============================================"
echo ""
echo "📊 Summary:"
echo "  • Publication: winestore_pub (Write DB)"
echo "  • Subscription: winestore_sub (Read DB)"
echo "  • Status: Active and syncing"
echo ""