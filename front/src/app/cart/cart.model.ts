export interface CartItem {
  productId: number;
  code: string;
  name: string;
  price: number;
  image?: string;
  quantity: number;
  max?: number;
}
