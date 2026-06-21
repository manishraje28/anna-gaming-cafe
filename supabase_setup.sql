-- ============================================================
-- ANNA GAMING CAFE — Complete Database Setup
-- Run this ENTIRE script in Supabase SQL Editor (one go)
-- ============================================================

-- 1. PROFILES (extends auth.users)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL DEFAULT '',
    email TEXT,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'OWNER')),
    loyalty_stars INT NOT NULL DEFAULT 0,
    wallet_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    referral_code TEXT UNIQUE,
    referred_by UUID REFERENCES profiles(id),
    avatar_url TEXT,
    total_visits INT NOT NULL DEFAULT 0,
    total_spend DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_visit_at TIMESTAMPTZ
);

-- 2. STATIONS
CREATE TABLE IF NOT EXISTS stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('PS5', 'PC')),
    status TEXT NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'BOOKED', 'MAINTENANCE')),
    price_per_hour DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    specs TEXT NOT NULL DEFAULT '',
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. GAMES
CREATE TABLE IF NOT EXISTS games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    platform TEXT NOT NULL CHECK (platform IN ('PS5', 'PC', 'ALL')),
    category TEXT NOT NULL DEFAULT '',
    cover_image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. PACKAGES
CREATE TABLE IF NOT EXISTS packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    duration_hours INT NOT NULL DEFAULT 1,
    base_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    perks TEXT DEFAULT '[]',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. PROMOTIONS
CREATE TABLE IF NOT EXISTS promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    badge_text TEXT NOT NULL DEFAULT '',
    badge_color TEXT NOT NULL DEFAULT '#00E5FF',
    stars_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    valid_from DATE,
    valid_until DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. BOOKINGS
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_ref TEXT NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    station_id UUID NOT NULL REFERENCES stations(id),
    package_id UUID REFERENCES packages(id),
    booking_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    duration_hours INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    payment_method TEXT CHECK (payment_method IN ('UPI', 'CASH', 'WALLET')),
    payment_status TEXT NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED')),
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. TRANSACTIONS
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id),
    type TEXT NOT NULL CHECK (type IN ('BOOKING_PAYMENT', 'WALLET_TOPUP', 'REFERRAL_BONUS', 'REFUND')),
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method TEXT CHECK (payment_method IN ('UPI', 'CASH', 'WALLET')),
    upi_transaction_id TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. REVIEWS
CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. MEMBERSHIPS
CREATE TABLE IF NOT EXISTS memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    tier TEXT NOT NULL DEFAULT 'BRONZE' CHECK (tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. CHAT ROOMS
CREATE TABLE IF NOT EXISTS chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_message_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. CHAT MESSAGES
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES profiles(id),
    content TEXT NOT NULL DEFAULT '',
    attachment_url TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_station ON bookings(station_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date ON bookings(booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_room ON chat_messages(room_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_customer ON chat_rooms(customer_id);
CREATE INDEX IF NOT EXISTS idx_stations_type ON stations(type);
CREATE INDEX IF NOT EXISTS idx_stations_status ON stations(status);

-- ============================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================

-- Security helper function to check owner role without RLS recursion
CREATE OR REPLACE FUNCTION public.is_owner(user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = user_id AND role = 'OWNER'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE stations ENABLE ROW LEVEL SECURITY;
ALTER TABLE games ENABLE ROW LEVEL SECURITY;
ALTER TABLE packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE promotions ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;

-- PROFILES policies
CREATE POLICY "Users can view own profile" ON profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Owners can view all profiles" ON profiles FOR SELECT USING (public.is_owner(auth.uid()));

-- STATIONS policies (everyone reads, only owners write)
CREATE POLICY "Anyone can view stations" ON stations FOR SELECT USING (TRUE);
CREATE POLICY "Owners can manage stations" ON stations FOR ALL USING (public.is_owner(auth.uid()));

-- GAMES policies (everyone reads)
CREATE POLICY "Anyone can view games" ON games FOR SELECT USING (TRUE);
CREATE POLICY "Owners can manage games" ON games FOR ALL USING (public.is_owner(auth.uid()));

-- PACKAGES policies (everyone reads)
CREATE POLICY "Anyone can view packages" ON packages FOR SELECT USING (TRUE);
CREATE POLICY "Owners can manage packages" ON packages FOR ALL USING (public.is_owner(auth.uid()));

-- PROMOTIONS policies (everyone reads)
CREATE POLICY "Anyone can view active promotions" ON promotions FOR SELECT USING (TRUE);
CREATE POLICY "Owners can manage promotions" ON promotions FOR ALL USING (public.is_owner(auth.uid()));

-- BOOKINGS policies
CREATE POLICY "Customers see own bookings" ON bookings FOR SELECT USING (customer_id = auth.uid());
CREATE POLICY "Customers can create bookings" ON bookings FOR INSERT WITH CHECK (customer_id = auth.uid());
CREATE POLICY "Owners see all bookings" ON bookings FOR SELECT USING (public.is_owner(auth.uid()));
CREATE POLICY "Owners can update bookings" ON bookings FOR UPDATE USING (public.is_owner(auth.uid()));

-- TRANSACTIONS policies
CREATE POLICY "Users see own transactions" ON transactions FOR SELECT USING (user_id = auth.uid());
CREATE POLICY "Users can create transactions" ON transactions FOR INSERT WITH CHECK (user_id = auth.uid());
CREATE POLICY "Owners see all transactions" ON transactions FOR SELECT USING (public.is_owner(auth.uid()));

-- REVIEWS policies
CREATE POLICY "Anyone can view reviews" ON reviews FOR SELECT USING (TRUE);
CREATE POLICY "Customers can create reviews" ON reviews FOR INSERT WITH CHECK (customer_id = auth.uid());

-- MEMBERSHIPS policies
CREATE POLICY "Users see own memberships" ON memberships FOR SELECT USING (user_id = auth.uid());
CREATE POLICY "Owners see all memberships" ON memberships FOR SELECT USING (public.is_owner(auth.uid()));

-- CHAT ROOMS policies
CREATE POLICY "Users see own chat rooms" ON chat_rooms FOR SELECT
    USING (customer_id = auth.uid() OR public.is_owner(auth.uid()));
CREATE POLICY "Users can create chat rooms" ON chat_rooms FOR INSERT WITH CHECK (customer_id = auth.uid());
CREATE POLICY "Owners can update chat rooms" ON chat_rooms FOR UPDATE USING (public.is_owner(auth.uid()));

-- CHAT MESSAGES policies
CREATE POLICY "Users see messages in their rooms" ON chat_messages FOR SELECT
    USING (
        EXISTS (SELECT 1 FROM chat_rooms WHERE chat_rooms.id = chat_messages.room_id AND chat_rooms.customer_id = auth.uid())
        OR public.is_owner(auth.uid())
    );
CREATE POLICY "Users can send messages" ON chat_messages FOR INSERT
    WITH CHECK (sender_id = auth.uid());


-- ============================================================
-- AUTO-CREATE PROFILE on signup (trigger)
-- ============================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, email, phone, referral_code)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data ->> 'full_name', ''),
        NEW.email,
        NEW.phone,
        'ANNA-' || UPPER(SUBSTR(MD5(NEW.id::text), 1, 6))
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================
-- SEED DATA: Stations
-- ============================================================
INSERT INTO stations (name, type, status, price_per_hour, specs, display_order) VALUES
    ('PS5 VIP Couch Suite 1', 'PS5', 'AVAILABLE', 250.00, 'DualSense Edge, 55" 4K Bravia OLED', 1),
    ('PS5 Lounge Station 2', 'PS5', 'AVAILABLE', 200.00, 'DualSense, 50" Bravia Ambient Setup', 2),
    ('Elite PC Rig - Desk A3', 'PC', 'AVAILABLE', 350.00, 'Core i9, RTX 4080, 240Hz ASUS PG27', 3),
    ('Pro Arena PC - Desk B1', 'PC', 'AVAILABLE', 450.00, 'Ryzen 9, RTX 4090, 360Hz Alienware', 4);

-- SEED DATA: Games
INSERT INTO games (title, platform, category) VALUES
    ('FIFA 24 Football Pro', 'PS5', 'Sports Simulation'),
    ('Cyberpunk 2077 NightCity', 'PC', 'Open-World RPG'),
    ('Valorant Tactical Arena', 'PC', 'Character-based FPS'),
    ('Elden Ring Shadow Lands', 'PS5', 'Challenging RPG'),
    ('TEKKEN 8 Iron Fist', 'PS5', 'Fighting Arcade'),
    ('Grand Theft Auto V', 'ALL', 'Open World RPG'),
    ('Counter-Strike 2 Global', 'PC', 'FPS Action'),
    ('Forza Horizon 5', 'PC', 'Racing Tour'),
    ('God of War Ragnarök', 'PS5', 'Action Adventure'),
    ('Apex Legends', 'ALL', 'Battle Royale');

-- SEED DATA: Packages
INSERT INTO packages (name, description, duration_hours, base_price, perks, display_order) VALUES
    ('Quick Session', '1 Hour Standard Gaming', 1, 250.00, '["Standard controller", "Basic setup"]', 1),
    ('VIP Bundle', '3 Hour Sessions + Complimentary Drink', 3, 650.00, '["Premium controller", "Drink included", "Priority support"]', 2),
    ('All-Nighter', '6 Hour Marathon + Energy Drinks', 6, 1100.00, '["Premium setup", "2 Energy drinks", "Snack pack", "Priority support"]', 3);

-- SEED DATA: Promotions
INSERT INTO promotions (title, description, badge_text, badge_color, stars_multiplier, is_active) VALUES
    ('Weekend FIFA Cup Special', 'Book any PS5 VIP couch station to receive 2x loyalty stars. Limited controllers remaining.', 'LIVE BONUS', '#FF007F', 2.00, TRUE),
    ('All-Night Energy Boost', 'Get the 6-hour Esports PC pass and check in before midnight for two free Monster drinks.', 'POPULAR', '#FF9E00', 1.50, TRUE);

-- ============================================================
-- ENABLE REALTIME for chat, bookings, stations
-- ============================================================
ALTER PUBLICATION supabase_realtime ADD TABLE chat_messages;
ALTER PUBLICATION supabase_realtime ADD TABLE bookings;
ALTER PUBLICATION supabase_realtime ADD TABLE stations;
