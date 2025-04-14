import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderByAlpha',
  standalone: true,
})
export class OrderByAlphaPipe implements PipeTransform {
  transform(value: any[]): any[] {
    return value.sort((a, b) => a.name.localeCompare(b.name));
  }
}
