export interface Pageable {
  pageNumber: number; // Numéro de la page demandée (commence souvent à 0)
  pageSize: number; // Nombre d’éléments par page
  offset: number; // Combien d’éléments on "saute" depuis le début (pageNumber * pageSize)

  sort: {
    empty: boolean; // Est-ce qu’on a un tri défini ?
    unsorted: boolean; // Est-ce qu’on n’a PAS trié ?
    sorted: boolean; // Est-ce qu’on a trié ? (opposé de unsorted)
  };

  unpaged: boolean; // Si `true`, alors pas de pagination (tout est retourné)
}

export interface Page<T> {
  content: T[]; // Le tableau d’éléments qu’on a reçu pour cette page

  pageable: Pageable; // Les infos sur comment la page a été générée (voir plus haut)
  totalPages: number; // Combien de pages il y a au total
  totalElements: number; // Nombre total d’éléments (toutes pages confondues)

  last: boolean; // Est-ce que c’est la dernière page ?
  first: boolean; // Est-ce que c’est la première page ?
  empty: boolean; // Est-ce qu’il n’y a aucun élément sur cette page ?

  size: number; // Combien d’éléments *par page*
  number: number; // Numéro de la page actuelle
  numberOfElements: number; // Nombre d’éléments retournés dans cette page (parfois < size si t’es à la fin)

  sort: {
    empty: boolean;
    unsorted: boolean;
    sorted: boolean;
  };
}
