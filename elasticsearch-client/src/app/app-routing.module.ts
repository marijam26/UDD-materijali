import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from "@angular/router";
import {HomepageComponent} from "./pages/homepage/homepage.component";
import {SearchPageComponent} from "./pages/search-page/search-page.component";



const routes: Routes = [
  {
    path: '',
    component: HomepageComponent,
  },
  {
    path: 'search',
    component: SearchPageComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
