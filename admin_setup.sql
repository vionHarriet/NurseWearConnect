-- Admin & System Setup SQL Script for NurseWearConnect

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. DROP EXISTING TABLES (CAUTION: Resets data for clean setup)
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.messages CASCADE;
DROP TABLE IF EXISTS public.order_items CASCADE;
DROP TABLE IF EXISTS public.orders CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;

-- 2. CREATE TABLES

-- Profiles Table
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users NOT NULL PRIMARY KEY,
  full_name TEXT,
  email TEXT UNIQUE,
  phone_number TEXT,
  role TEXT DEFAULT 'student' CHECK (role IN ('student', 'vendor', 'admin')),
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'pending', 'suspended')),
  business_name TEXT,
  location TEXT,
  business_description TEXT,
  avatar_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Products Table
CREATE TABLE public.products (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  vendor_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  gender TEXT CHECK (gender IN ('Male', 'Female', 'Unisex')),
  price_kes INTEGER NOT NULL,
  rating NUMERIC DEFAULT 0.0,
  reviews_count INTEGER DEFAULT 0,
  tag TEXT,
  images TEXT[] DEFAULT '{}',
  emoji TEXT,
  description TEXT,
  in_stock BOOLEAN DEFAULT TRUE,
  available_sizes TEXT[] DEFAULT '{"XS", "S", "M", "L", "XL", "XXL"}',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Orders Table
CREATE TABLE public.orders (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES public.profiles(id) NOT NULL,
  total_amount NUMERIC NOT NULL,
  shipping_address TEXT NOT NULL,
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Order Items Table
CREATE TABLE public.order_items (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  order_id UUID REFERENCES public.orders(id) ON DELETE CASCADE NOT NULL,
  product_id UUID REFERENCES public.products(id) NOT NULL,
  quantity INTEGER NOT NULL,
  size TEXT,
  price_at_purchase NUMERIC NOT NULL
);

-- Notifications Table
CREATE TABLE public.notifications (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
  title TEXT NOT NULL,
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. ENABLE RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- 4. RLS POLICIES (ADMIN LEVEL ACCESS)

-- Admin Policy: Admins can do anything on all tables
CREATE POLICY "Admins have full access" ON public.profiles FOR ALL TO authenticated USING (
  (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'admin'
);

CREATE POLICY "Admins can manage products" ON public.products FOR ALL TO authenticated USING (
  (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'admin'
);

CREATE POLICY "Admins can manage orders" ON public.orders FOR ALL TO authenticated USING (
  (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'admin'
);

-- Public/Student Policies
CREATE POLICY "Profiles are viewable by self" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Products are viewable by everyone" ON public.products FOR SELECT USING (true);
CREATE POLICY "Orders viewable by owners" ON public.orders FOR SELECT USING (auth.uid() = user_id);

-- 5. AUTOMATION TRIGGERS

-- Automatically create profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, email, role, status)
  VALUES (
    NEW.id,
    NEW.raw_user_meta_data->>'full_name',
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'role', 'student'),
    CASE WHEN (NEW.raw_user_meta_data->>'role') = 'vendor' THEN 'pending' ELSE 'active' END
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- 6. SEED SYSTEM ADMIN
-- Note: Password is 'Admin@2024' (hashed)
-- We insert into auth.users first, then the trigger handles public.profiles
-- But since we can't easily trigger auth.users from here without admin privileges,
-- we'll provide the query to run in the Supabase Dashboard or via CLI.

-- MOCK DATA FOR TESTING ADMIN SCREENS
INSERT INTO public.profiles (id, full_name, email, role, status)
VALUES
('00000000-0000-0000-0000-000000000000', 'System Administrator', 'admin@nursewear.com', 'admin', 'active')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.products (name, category, price_kes, emoji, in_stock)
VALUES
('Classic Navy Scrubs', 'Top', 2500, '👕', true),
('Professional Lab Coat', 'Outerwear', 4500, '🥼', true);
