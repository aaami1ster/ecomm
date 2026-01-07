# Requirements
1. Place orders for multiple products
1. Validate stock; reject if insufficient
1. Decrease product inventory upon successful order
1. Each order includes items ( productId , quantity , unitPrice ,
1. discountApplied , totalPrice ) and orderTotal

1. Discount Rules:
   - USER : no discount
   - PREMIUM_USER : 10% off total order
   - Orders > $500: extra 5% discount for any user
1. Implement discount calculation dynamically using a suitable design pattern