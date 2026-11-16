class OrderItem {
  constructor(menuItem, quantity, selectedOptions = []) {
    this.id = menuItem.id; this.itemId = menuItem.id;
    this.itemName = menuItem.name; this.unitPrice = menuItem.price;
    this.quantity = quantity; this.selectedOptions = selectedOptions;
  }
  getSubtotal() { return Math.round(this.unitPrice * 100) * this.quantity / 100; }
}
module.exports = OrderItem;
