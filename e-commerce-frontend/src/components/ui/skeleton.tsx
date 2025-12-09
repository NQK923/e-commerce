import { cx } from "@/src/utils/cx"

function Skeleton({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cx("animate-pulse rounded-md bg-zinc-200", className)}
      {...props}
    />
  )
}

export { Skeleton }