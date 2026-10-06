delete from review_item
where status = 'PENDING'
  and exists (
      select 1 from review_item other
      where other.lab_id = review_item.lab_id
        and other.status = 'PENDING'
        and other.id <> review_item.id
        and (other.repetition_stage > review_item.repetition_stage
             or (other.repetition_stage = review_item.repetition_stage and other.created_at > review_item.created_at)
             or (other.repetition_stage = review_item.repetition_stage and other.created_at = review_item.created_at
                 and other.id > review_item.id))
  );
