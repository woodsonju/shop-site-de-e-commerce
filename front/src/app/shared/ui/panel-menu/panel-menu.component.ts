import {Component, inject} from "@angular/core";
import { MenuItem } from "primeng/api";
import { PanelMenuModule } from 'primeng/panelmenu';
import {Router, RouterModule} from '@angular/router';
import {TokenService} from "../../../auth/token.service";
import {AuthService} from "../../../auth/auth.service";


  @Component({
    selector: "app-panel-menu",
    standalone: true,
    imports: [PanelMenuModule],
    template: `
        <p-panelMenu [model]="items" styleClass="w-full" />
    `
  })
  export class PanelMenuComponent {

    private readonly auth = inject(AuthService);
    private readonly token = inject(TokenService);
    private readonly router = inject(Router);

    public readonly items: MenuItem[] = [
        {
            label: 'Accueil',

            icon: 'pi pi-home',
            routerLink: ['/home']
        },
        {
            label: 'Produits',
            icon: 'pi pi-barcode',
            routerLink: ['/products/list']
        },
      {
        label: 'Auth',
        icon: 'pi pi-user',
        items: [
          { label: 'Login', icon: 'pi pi-sign-in', routerLink: ['/auth/login'] },
          { label: 'Register', icon: 'pi pi-user-plus', routerLink: ['/auth/register'] },
          { label: 'Activate', icon: 'pi pi-check-circle', routerLink: ['/auth/activate-account'] },
          // 👇 Ajout de l'item Logout (affiché uniquement si un token valide existe)
          {
            label: 'Logout',
            icon: 'pi pi-sign-out',
            visible: this.token.isTokenValid(),
            command: () => this.onLogout()
          }
        ]
      }
    ]

    /** Déconnexion + redirection vers la page de login */
    private onLogout(): void {
      this.auth.logout();
      // Optionnel : toast de confirmation si tu utilises MessageService
      // this.messageService.add({ severity: 'info', summary: 'Logout', detail: 'Vous avez été déconnecté.' });

      // Met à jour l’affichage du menu pour cacher le bouton Logout
      this.items[2].items!.find(i => i.label === 'Logout')!.visible = false;
      this.router.navigate(['/auth/login']);
    }
  }
