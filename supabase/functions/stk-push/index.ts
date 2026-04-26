import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const MPESA_CONSUMER_KEY = Deno.env.get('MPESA_CONSUMER_KEY')
const MPESA_CONSUMER_SECRET = Deno.env.get('MPESA_CONSUMER_SECRET')
const MPESA_SHORTCODE = Deno.env.get('MPESA_SHORTCODE')
const MPESA_PASSKEY = Deno.env.get('MPESA_PASSKEY')
const MPESA_CALLBACK_URL = Deno.env.get('MPESA_CALLBACK_URL')

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { orderId, phoneNumber, amount } = await req.json()

    // 1. Get Access Token
    const auth = btoa(`${MPESA_CONSUMER_KEY}:${MPESA_CONSUMER_SECRET}`)
    const tokenResp = await fetch("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials", {
      headers: { Authorization: `Basic ${auth}` }
    })
    const { access_token } = await tokenResp.json()

    // 2. Generate Password
    const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, 14)
    const password = btoa(`${MPESA_SHORTCODE}${MPESA_PASSKEY}${timestamp}`)

    // 3. Initiate STK Push
    const stkResp = await fetch("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest", {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${access_token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        BusinessShortCode: MPESA_SHORTCODE,
        Password: password,
        Timestamp: timestamp,
        TransactionType: "CustomerPayBillOnline",
        Amount: Math.round(amount),
        PartyA: phoneNumber,
        PartyB: MPESA_SHORTCODE,
        PhoneNumber: phoneNumber,
        CallBackURL: MPESA_CALLBACK_URL,
        AccountReference: `Order ${orderId}`,
        TransactionDesc: "NurseWear Connect Purchase"
      })
    })

    const result = await stkResp.json()

    return new Response(JSON.stringify(result), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 200,
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
