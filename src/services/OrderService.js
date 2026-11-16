const OrderItem = require('../models/OrderItem');
class OrderService {
  constructor(database) { this.database = database; }
  validateCart(cart) { return Array.isArray(cart) && cart.length >= 1 && cart.length <= 50; }
  createOrder(cart) {
    if (!this.validateCart(cart)) throw new RangeError('Cart must contain 1 to 50 items');
    let subtotalCents = 0;
    const items = cart.map(raw => {
      if (!raw || typeof raw !== 'object' || Array.isArray(raw)) throw new RangeError('Invalid cart item');
      const id = raw.id ?? raw.itemId;
      const quantity = raw.quantity;
      if (!Number.isSafeInteger(id) || id < 1) throw new RangeError('Invalid menu item ID');
      if (!Number.isSafeInteger(quantity) || quantity < 1 || quantity > 99) throw new RangeError('Quantity must be 1 to 99');
      const menuItem = this.database.getMenu().findItem(id);
      if (!menuItem) throw new RangeError(`Unknown menu item: ${id}`);
      const options = raw.selectedOptions ?? [];
      if (!Array.isArray(options) || options.length > 20 || options.some(x => typeof x !== 'string' || x.length > 100))
        throw new RangeError('Invalid customization options');
      const item = new OrderItem(menuItem, quantity, options);
      subtotalCents += Math.round(menuItem.price * 100) * quantity;
      if (!Number.isSafeInteger(subtotalCents) || subtotalCents > 100000000) throw new RangeError('Order total too large');
      return item;
    });
    // Demonstration tax only, not location-specific sales tax.
    const taxCents = Math.round(subtotalCents * 0.05);
    return this.database.saveOrder(items, subtotalCents / 100, taxCents / 100, (subtotalCents + taxCents) / 100);
  }
}
module.exports = OrderService;
