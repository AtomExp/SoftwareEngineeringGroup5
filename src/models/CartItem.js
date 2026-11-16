class CartItem {
  constructor(menuItem, quantity = 1, selectedOptions = []) {
    this.menuItem = menuItem; this.quantity = quantity; this.selectedOptions = selectedOptions;
  }
  getSubtotal() { return Math.round(this.menuItem.price * 100) * this.quantity / 100; }
}
module.exports = CartItem;
