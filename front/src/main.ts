import { enableProdMode, importProvidersFrom } from "@angular/core";

import { registerLocaleData } from "@angular/common";
import {
  HTTP_INTERCEPTORS,
  provideHttpClient, withInterceptors,
  withInterceptorsFromDi,
} from "@angular/common/http";
import localeFr from "@angular/common/locales/fr";
import { BrowserModule, bootstrapApplication } from "@angular/platform-browser";
import { provideAnimations } from "@angular/platform-browser/animations";
import { provideRouter } from "@angular/router";
import { APP_ROUTES } from "app/app.routes";
import { ConfirmationService, MessageService } from "primeng/api";
import { DialogService } from "primeng/dynamicdialog";
import { AppComponent } from "./app/app.component";
import { environment } from "./environments/environment";
import {HttpTokenInterceptor} from "./app/auth/http-token.interceptor";
import {JWT_OPTIONS, JwtHelperService, JwtModule} from "@auth0/angular-jwt";



if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      BrowserModule,
      // 👇 Fournit JwtHelperService + JWT_OPTIONS à la DI
      JwtModule.forRoot({
        jwtOptionsProvider: {
          provide: JWT_OPTIONS,
          useFactory: () => ({
            tokenGetter: () => localStorage.getItem('token'),
            // (optionnel) allowedDomains, disallowedRoutes, etc.
          }),
        },
      })
    ),
   /* provideHttpClient(
      withInterceptorsFromDi(),
    )*/
    //Très important pour les interceptors "classe"
    provideHttpClient(withInterceptorsFromDi()),

    //Enregistre l’interceptor en DI
    { provide: HTTP_INTERCEPTORS, useClass: HttpTokenInterceptor, multi: true },
    provideAnimations(),
    provideRouter(APP_ROUTES),
    ConfirmationService,
    MessageService,
    DialogService,
    JwtHelperService,
  ],
}).catch((err) => console.log(err));

registerLocaleData(localeFr, "fr-FR");
