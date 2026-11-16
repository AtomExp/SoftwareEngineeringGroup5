const CartItem = require('../models/CartItem');
class OrderingController {
  constructor(menu, cart, orderService) { Object.assign(this, { menu, cart, orderService }); }
  onAddItem(itemId, qty) {
    const food = this.menu.findItem(itemId);
    if (!food) throw new Error('Unknown menu item');
    this.cart.addItem(new CartItem(food, qty));
  }
  onUpdateCart(itemId, qty) { this.cart.updateQuantity(itemId, qty); }
  onSubmitOrder() {
    return this.orderService.createOrder(this.cart.items.map(item => ({id: item.menuItem.id, quantity: item.quantity})));
  }
}
module.exports = OrderingController;
