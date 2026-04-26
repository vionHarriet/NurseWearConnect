import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const SUPABASE_URL = Deno.env.get('SUPABASE_URL')
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')

serve(async (req) => {
  const supabase = createClient(SUPABASE_URL!, SUPABASE_SERVICE_ROLE_KEY!)

  try {
    const body = await req.json()
    const { Body: { stkCallback } } = body

    const checkoutRequestId = stkCallback.CheckoutRequestID
    const resultCode = stkCallback.ResultCode
    const resultDesc = stkCallback.ResultDesc

    let status = 'failed'
    if (resultCode === 0) {
      status = 'paid'
      const mpesaReceiptNumber = stkCallback.CallbackMetadata.Item.find((i: any) => i.Name === 'MpesaReceiptNumber').Value

      // Update order status in database
      await supabase
        .from('orders')
        .update({
            status: 'paid',
            payment_details: {
                receipt: mpesaReceiptNumber,
                checkout_id: checkoutRequestId,
                callback_desc: resultDesc
            }
        })
        .eq('payment_id', checkoutRequestId) // We should store checkoutRequestId when initiating
    } else {
        await supabase
        .from('orders')
        .update({
            status: 'payment_failed',
            payment_details: {
                checkout_id: checkoutRequestId,
                callback_desc: resultDesc,
                result_code: resultCode
            }
        })
        .eq('payment_id', checkoutRequestId)
    }

    return new Response(JSON.stringify({ message: "Callback received" }), {
      headers: { 'Content-Type': 'application/json' },
      status: 200,
    })

  } catch (error) {
    console.error('Callback error:', error)
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
