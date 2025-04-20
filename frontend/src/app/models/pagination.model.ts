export interface Pageable {
  pageNumber: number; // Page number requested (often starts at 0)
  pageSize: number; // Number of items per page
  offset: number; // How many items are skipped from the start (pageNumber * pageSize)

  sort: {
    empty: boolean; // Is a sort defined?
    unsorted: boolean; // Is it not sorted?
    sorted: boolean; // Is it sorted? (opposite of unsorted)
  };

  unpaged: boolean; // If `true`, then no pagination (everything is returned)
}

export interface Page<T> {
  content: T[]; // The array of items received for this page

  pageable: Pageable; // Information on how the page was generated (see above)
  totalPages: number; // Total number of pages
  totalElements: number; // Total number of elements (across all pages)

  last: boolean; // Is this the last page?
  first: boolean; // Is this the first page?
  empty: boolean; // Are there no elements on this page?

  size: number; // Number of elements *per page*
  number: number; // Current page number
  numberOfElements: number; // Number of elements returned in this page (sometimes < size if at the end)

  sort: {
    empty: boolean;
    unsorted: boolean;
    sorted: boolean;
  };
}
