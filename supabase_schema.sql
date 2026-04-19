-- Create tables for NurseWearConnect

-- 1. Profiles Table (Extends Auth.Users)
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users NOT NULL PRIMARY KEY,
  full_name TEXT,
  email TEXT,
  phone_number TEXT,
  role TEXT DEFAULT 'student' CHECK (role IN ('student', 'professional', 'vendor', 'admin')),
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'pending', 'suspended')),
  business_name TEXT,
  location TEXT,
  business_description TEXT,
  avatar_url TEXT,
  measurements JSONB DEFAULT '{"bust": "0", "waist": "0", "hips": "0"}',
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Products Table
CREATE TABLE public.products (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  vendor_id UUID REFERENCES auth.users,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  gender TEXT CHECK (gender IN ('Male', 'Female', 'Unisex')),
  price_kes INTEGER NOT NULL,
  rating NUMERIC DEFAULT 0.0,
  reviews_count INTEGER DEFAULT 0,
  tag TEXT,
  images TEXT[] DEFAULT '{}',
  description TEXT,
  material TEXT DEFAULT 'High-quality, breathable fabric designed for all-day comfort.',
  features TEXT[] DEFAULT '{}',
  in_stock BOOLEAN DEFAULT TRUE,
  available_sizes TEXT[] DEFAULT '{"XS", "S", "M", "L", "XL", "XXL"}',
  available_colors JSONB DEFAULT '[]',
  sub_category TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Orders Table
CREATE TABLE public.orders (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  total_amount NUMERIC NOT NULL,
  shipping_address TEXT NOT NULL,
  currency TEXT DEFAULT 'KES',
  status TEXT DEFAULT 'pending',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Order Items Table
CREATE TABLE public.order_items (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  order_id UUID REFERENCES public.orders ON DELETE CASCADE NOT NULL,
  product_id UUID REFERENCES public.products NOT NULL,
  quantity INTEGER NOT NULL,
  size TEXT,
  color TEXT,
  price_at_purchase NUMERIC NOT NULL
);

-- 7. Categories Table
CREATE TABLE public.categories (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 8. Coupons Table
CREATE TABLE public.coupons (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  discount_type TEXT CHECK (discount_type IN ('percentage', 'fixed')),
  discount_value NUMERIC NOT NULL,
  expiry_date TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 9. Banners Table
CREATE TABLE public.banners (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  image_url TEXT NOT NULL,
  link_url TEXT,
  title TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Messages Table
CREATE TABLE public.messages (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  sender_id UUID REFERENCES auth.users NOT NULL,
  receiver_id UUID REFERENCES auth.users, -- NULL if system/admin
  content TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Notifications Table
CREATE TABLE public.notifications (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  title TEXT NOT NULL,
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 10. System Logs Table
CREATE TABLE public.system_logs (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users,
  action TEXT NOT NULL,
  details TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.system_logs ENABLE ROW LEVEL SECURITY;

-- RLS Policies

-- System Logs: Only admins can view logs
CREATE POLICY "Admins can view logs" ON public.system_logs
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM public.profiles
      WHERE profiles.id = auth.uid()
      AND profiles.role = 'admin'
    )
  );

-- Products: Everyone can view, only admins can modify
CREATE POLICY "Anyone can view products" ON public.products
  FOR SELECT USING (true);

-- Orders: Users can view and create their own orders
CREATE POLICY "Users can view own orders" ON public.orders
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can create own orders" ON public.orders
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Order Items: Users can view their own order items
CREATE POLICY "Users can view own order items" ON public.order_items
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM public.orders
      WHERE orders.id = order_items.order_id
      AND orders.user_id = auth.uid()
    )
  );

-- Messages: Users can see messages they sent or received
CREATE POLICY "Users can view own messages" ON public.messages
  FOR SELECT USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

CREATE POLICY "Users can send messages" ON public.messages
  FOR INSERT WITH CHECK (auth.uid() = sender_id);

-- Notifications: Users can see and update their own notifications
CREATE POLICY "Users can view own notifications" ON public.notifications
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can update own notifications" ON public.notifications
  FOR UPDATE USING (auth.uid() = user_id);

-- Functions and Triggers for Profile Creation
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
  user_role TEXT;
  user_status TEXT;
BEGIN
  -- Extract role from metadata, default to 'student'
  user_role := COALESCE(new.raw_user_meta_data->>'role', 'student');

  -- Vendors start as 'pending', others 'active'
  IF user_role = 'vendor' THEN
    user_status := 'pending';
  ELSE
    user_status := 'active';
  END IF;

  INSERT INTO public.profiles (id, full_name, email, role, status)
  VALUES (
    new.id,
    new.raw_user_meta_data->>'full_name',
    new.email,
    user_role,
    user_status
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to notify admin of new vendor registration
CREATE OR REPLACE FUNCTION public.notify_admin_new_vendor()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.role = 'vendor' AND NEW.status = 'pending' THEN
    INSERT INTO public.notifications (user_id, title, content)
    SELECT id, 'New Vendor Registration', 'A new vendor ' || NEW.full_name || ' has registered and is awaiting approval.'
    FROM public.profiles WHERE role = 'admin';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_vendor_pending
  AFTER INSERT ON public.profiles
  FOR EACH ROW EXECUTE PROCEDURE public.notify_admin_new_vendor();

-- Trigger to notify vendor of approval
CREATE OR REPLACE FUNCTION public.notify_vendor_status_change()
RETURNS TRIGGER AS $$
BEGIN
  IF OLD.status = 'pending' AND NEW.status = 'active' THEN
    INSERT INTO public.notifications (user_id, title, content)
    VALUES (NEW.id, 'Account Approved', 'Your vendor account has been approved. You can now start listing products!');
  ELSIF NEW.status = 'suspended' THEN
    INSERT INTO public.notifications (user_id, title, content)
    VALUES (NEW.id, 'Account Suspended', 'Your account has been suspended. Please contact support for more details.');
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_vendor_approval
  AFTER UPDATE OF status ON public.profiles
  FOR EACH ROW EXECUTE PROCEDURE public.notify_vendor_status_change();

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- Initial Admin Account (example)
-- To be run manually or via a secure admin setup script
-- INSERT INTO auth.users (id, email, password, raw_user_meta_data)
-- VALUES (gen_random_uuid(), 'admin@nursewear.com', crypt('Admin@123', gen_salt('bf')), '{"full_name": "System Admin"}');

-- Note: In Supabase, use the dashboard or API to create the auth user first, 
-- then the trigger will create the profile, and then you can update the role.
-- UPDATE public.profiles SET role = 'admin' WHERE email = 'admin@nursewear.com';

