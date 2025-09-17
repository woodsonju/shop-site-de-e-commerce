import {Component, OnInit, inject, signal, computed} from "@angular/core";
import { Product } from "app/products/data-access/product.model";
import { ProductsService } from "app/products/data-access/products.service";
import { ProductFormComponent } from "app/products/ui/product-form/product-form.component";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { DataViewModule } from 'primeng/dataview';
import { DialogModule } from 'primeng/dialog';
import {MessageModule} from "primeng/message";
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {PaginatorModule, PaginatorState} from "primeng/paginator";

const emptyProduct: Product = {
  id: 0,
  code: '',
  name: '',
  description: '',
  image: '',
  category: '',
  price: 0,
  quantity: 0,
  internalReference: '',
  shellId: 0,
  inventoryStatus: 'INSTOCK',
  rating: 0,
  createdAt: 0,
  updatedAt: 0,
};

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
  standalone: true,
  imports: [DataViewModule, CardModule, ButtonModule, DialogModule, ProductFormComponent, MessageModule, ProgressSpinnerModule, PaginatorModule],
})
export class ProductListComponent implements OnInit {
  private readonly productsService = inject(ProductsService);

  // données + états exposés par le service (optionnel mais recommandé)
  public readonly products = this.productsService.products;
  public readonly loading = this.productsService.loading;   // <- pour un spinner
  public readonly error = this.productsService.error;       // <- pour afficher une erreur globale

  /** Page courante (métadonnées de pagination côté back) */
  public readonly page = this.productsService.page;

  public isDialogVisible = false;
  public isCreation = false;
  public readonly editedProduct = signal<Product>(emptyProduct);

  /** Nombre d’éléments par page par défaut (aligné avec le service) */
  private readonly defaultRows = 12;

  /**
   * totalRecords — valeur à passer au paginator (totalElements renvoyés par le back)
   *  - computed pour qu’elle réagisse automatiquement aux changements de page()
   */
  public readonly totalRecords = computed(() => this.page()?.totalElements ?? 0);

  /**
   * rows — nombre d’éléments par page affichés (issu de la dernière page reçue, ou valeur par défaut)
   */
  public readonly rows = computed(() => this.page()?.size ?? this.defaultRows);

  /**
   * first — index du premier enregistrement de la page pour le paginator
   *  - PrimeNG utilise `first` pour positionner la pagination (pageIndex * rows)
   */
  public readonly first = computed(() => {
    const p = this.page();
    if (!p) return 0;
    return p.number * p.size;
  });


  /**
   * ngOnInit — charge la première page de produits au montage.
   */
  ngOnInit(): void {
    this.productsService.list({ page: 0, size: this.defaultRows }).subscribe();
  }

  public onCreate() {
    this.isCreation = true;
    this.isDialogVisible = true;
    this.editedProduct.set(emptyProduct);
  }

  public onUpdate(product: Product) {
    this.isCreation = false;
    this.isDialogVisible = true;
    this.editedProduct.set(product);
  }

  /**
   * onDelete — supprime un produit puis recharge la page courante
   * @param product produit à supprimer
   */
  public onDelete(product: Product): void {
    // 🔁 On recharge la page active après suppression pour rester cohérent
    const currentPage = this.page()?.number ?? 0;
    const currentSize = this.page()?.size ?? this.defaultRows;

    this.productsService.delete(product.id).subscribe({
      next: () => this.productsService.list({ page: currentPage, size: currentSize }).subscribe(),
    });
  }

  /**
   * onSave — crée ou met à jour un produit, ferme la modale, puis recharge la page courante
   * @param product données du formulaire
   */
  public onSave(product: Product): void {
    const currentPage = this.page()?.number ?? 0;
    const currentSize = this.page()?.size ?? this.defaultRows;

    if (this.isCreation) {
      this.productsService.create(product).subscribe({
        next: () => {
          // succès : on ferme et on recharge
          this.isDialogVisible = false;
          this.productsService.list({ page: currentPage, size: currentSize }).subscribe();
        },
        error: (err) => {

        },
      });
    } else {
      this.productsService.update(product.id, product).subscribe({
        next: () => {
          this.isDialogVisible = false;
          this.productsService.list({ page: currentPage, size: currentSize }).subscribe();
        },
        error: () => {
          // idem : on ne ferme pas sur erreur
        },
      });
    }
  }
/*  public onSave(product: Product): void {
    const currentPage = this.page()?.number ?? 0;
    const currentSize = this.page()?.size ?? this.defaultRows;

    const after = () => {
      this.isDialogVisible = false;
      this.productsService.list({ page: currentPage, size: currentSize }).subscribe();
    };

    if (this.isCreation) {
      this.productsService.create(product).subscribe({ next: after, error: after });
    } else {
      this.productsService.update(product.id, product).subscribe({ next: after, error: after });
    }
  }*/

  /**
   * onCancel — ferme la modale sans action
   */
  public onCancel(): void {
    this.closeDialog();
  }

  /**
   * onPageChange — handler appelé par le paginator PrimeNG
   *  - `event.page` : index de la page (0-based)
   *  - `event.rows` : nombre d’éléments par page
   *  → Recharge la liste côté service avec `list({ page, size })`.
   */
  public onPageChange(event: PaginatorState): void {
    this.productsService.list({ page: event.page, size: event.rows }).subscribe();
  }

  /**
   * closeDialog — utilitaire simple pour fermer la modale
   */
  private closeDialog(): void {
    this.isDialogVisible = false;
  }



/*const emptyProduct: Product = {
  id: 0,
  code: "",
  name: "",
  description: "",
  image: "",
  category: "",
  price: 0,
  quantity: 0,
  internalReference: "",
  shellId: 0,
  inventoryStatus: "INSTOCK",
  rating: 0,
  createdAt: 0,
  updatedAt: 0,
};

@Component({
  selector: "app-product-list",
  templateUrl: "./product-list.component.html",
  styleUrls: ["./product-list.component.scss"],
  standalone: true,
  imports: [DataViewModule, CardModule, ButtonModule, DialogModule, ProductFormComponent],
})
export class ProductListComponent implements OnInit {
  private readonly productsService = inject(ProductsService);

  public readonly products = this.productsService.products;

  public isDialogVisible = false;
  public isCreation = false;
  public readonly editedProduct = signal<Product>(emptyProduct);

  ngOnInit() {
    this.productsService.get().subscribe();
  }

  public onCreate() {
    this.isCreation = true;
    this.isDialogVisible = true;
    this.editedProduct.set(emptyProduct);
  }

  public onUpdate(product: Product) {
    this.isCreation = false;
    this.isDialogVisible = true;
    this.editedProduct.set(product);
  }

  public onDelete(product: Product) {
    this.productsService.delete(product.id).subscribe();
  }

  public onSave(product: Product) {
    if (this.isCreation) {
      this.productsService.create(product).subscribe();
    } else {
      this.productsService.update(product).subscribe();
    }
    this.closeDialog();
  }

  public onCancel() {
    this.closeDialog();
  }

  private closeDialog() {
    this.isDialogVisible = false;
  }*/
}
