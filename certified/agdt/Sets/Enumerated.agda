module Sets.Enumerated where

-- Sets.Enummerate 枚举类型
data Bool : Set where
  true  : Bool
  false : Bool

data Answer : Set where
  yes   : Answer
  no    : Answer
  maybe : Answer

data Quarter : Set where
  east  : Quarter
  west  : Quarter
  north : Quarter
  south : Quarter
