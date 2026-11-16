class ShoppingCart {
  constructor(cartId = 1) { this.cartId = cartId; this.items = []; }
  addItem(item) { this.items.push(item); }
  removeItem(itemId) { this.items = this.items.filter(item => item.menuItem.id !== itemId); }
  updateQuantity(itemId, qty) {
    const item = this.items.find(item => item.menuItem.id === itemId);
    if (item) { if (qty <= 0) this.removeItem(itemId); else item.quantity = qty; }
  }
  calculateTotal() { return this.items.reduce((sum, item) => sum + Math.round(item.getSubtotal() * 100), 0) / 100; }
}
module.exports = ShoppingCart;
