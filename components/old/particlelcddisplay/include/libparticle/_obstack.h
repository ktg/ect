#include "conf.h"
#include <stdlib.h>
#include <assert.h>

/** @brief Structure for a chunk (part of an obstack) */
struct _chunk
{
	/** current leght of the data stored in this chunk */
	int						level;
	/** data of this chunk */
	char					data[OBSTACK_CHUNK_SIZE];
	/** pointer to the next chunk */
	TAILQ_ENTRY(_chunk)			next;
};

TAILQ_HEAD(_obstack, _chunk);

/** @brief Adding data to an obstack.
 *
 * This function adds data to an obstack.
 *
 * @param ob Pointer to the obstack, where data should be stored.
 * @param data Pointer to the data, which should be stored in the obstack.
 * @param len Lenght of data.
 * @return lenght of the added data.
 */
static char *_obstack_insert(struct _obstack * ob, uint8_t * data, uint8_t len)
{
  struct _chunk * last = TAILQ_LAST(ob, _obstack);
  char *dest;

  assert( ((uint16_t)len)+1 <= OBSTACK_CHUNK_SIZE && "OBSTACK_CHUNK_SIZE too small for application");

  if((!last) || (last->level + len) > OBSTACK_CHUNK_SIZE)
  {
    last=malloc(sizeof(struct _chunk));
    last->level=0;
    TAILQ_INSERT_TAIL(ob, last, next);
  }
  dest=last->data+last->level;

  last->level += len;
  return memcpy(dest, data, len);
}

/** @brief This function gets the length of the data stored in an obstack.
 *
 * @param ob Pointer to the obstack, where data is stored.
 * @return lenght of the data stored in the obstack.
 */
static long _obstack_get_length(struct _obstack * ob)
{
  struct _chunk * chunk;
  long len = 0;

  TAILQ_FOREACH(chunk,ob,next)
  {
    len += chunk->level;
  }

  return len;
}

/** @brief Copy data from an obstack into a buffer.
 *
 * This function copys the data, which is stored in an obstack, into one single buffer.
 * The buffer has to be freed by the caller.
 * The ostack is empty on completion.
 * @param ob Pointer to the obstack, where the data is stord in.
 * @return buf Pointer to the buffer, where the data will be copyed to.
 */
static char *_obstack_finish(struct _obstack * ob)
{
  long position = 0;
  struct _chunk *chunk,*tmp;
  char *buf;

  if(buf=malloc(_obstack_get_length(ob)))
  TAILQ_FOREACH_SAFE(chunk,ob,next,tmp)
  {
    memcpy(buf+position, chunk->data, chunk->level);
    position += chunk->level;
    TAILQ_REMOVE(ob,chunk,next);
	free(chunk);
  }
  return buf;
}

static void _obstack_free(struct _obstack * ob,char * level)
{
  long position = 0;
  struct _chunk *chunk,*tmp;
  char *buf;
    
  if(buf=malloc(_obstack_get_length(ob)))

  TAILQ_FOREACH_SAFE(chunk,ob,next,tmp)
  {
    if(!level)
    {
      TAILQ_REMOVE(ob,chunk,next);
      free(chunk);
    }
    else if((chunk->data < level) && (level < chunk->data + chunk->level))
    {
      chunk->level=level-chunk->data;
      level=NULL;
    }
  }
}
